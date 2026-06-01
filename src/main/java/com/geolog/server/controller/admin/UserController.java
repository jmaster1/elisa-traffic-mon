package com.geolog.server.controller.admin;

import com.geolog.server.config.GeoPreferences;
import com.geolog.server.controller.Mapping;
import com.geolog.server.model.clientprofile.ClientProfile;
import com.geolog.server.model.clientprofile.ClientProfileService;
import com.geolog.server.model.device.Device;
import com.geolog.server.model.device.DeviceService;
import com.geolog.server.model.session.ClientSession;
import com.geolog.server.model.session.ClientSessionService;
import com.geolog.server.model.userworksite.UserWorksiteService;
import jakarta.validation.constraints.Size;
import jmaster.core.controller.AbstractEntityController;
import jmaster.core.controller.CreatePageStateParams;
import jmaster.core.controller.EntityControllerConfig;
import jmaster.core.model.AbstractEntity;
import jmaster.core.model.AbstractVersionedEntity;
import jmaster.core.service.EntityIO;
import jmaster.core.ui.FormState;
import jmaster.core.ui.PageState;
import jmaster.core.ui.annot.Ui;
import jmaster.system.user.User;
import jmaster.system.user.UserFilter;
import jmaster.system.user.UserRole;
import jmaster.system.user.UserService;
import jmaster.system.prefs.PrefsService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Controller
@RequestMapping(Mapping.Admin.USER)
@RequiredArgsConstructor
public class UserController extends AbstractEntityController<Long, User, UserFilter, UserDetails> {

    static final String[] EXC_FIELDS = Stream.concat(
            Stream.of(AbstractVersionedEntity.FIELDS_ALL_BUT_CREATED),
            Stream.of(User.Fields.uid))
                    .toArray(String[]::new);

    private static final Set<UserRole> USER_ROLES_CLIENT = Set.of(UserRole.client);

    private final UserService userService;

    private final DeviceService deviceService;

    private final ClientSessionService clientSessionService;

    private final UserWorksiteService userWorksiteService;

    private final ClientProfileService clientProfileService;

    private final PrefsService prefsService;

    @Override
    protected void configure(EntityControllerConfig<User, UserFilter> config) {
        config.filterDefaultSort(User.Fields.name, UserFilter.SORT_ASC);
        config.listTemplate(getListTemplateSpecific());
        config.pageState(PageState::doHideFilter);
        config.pageModel(model ->
                model.excludeProperties(EXC_FIELDS)
                .excludeProperties(User.Fields.email)
                .setLinkSupplier(User.Fields.name, getEditLinkSupplier()));

        config.editTemplate(getEditTemplateSpecific());
        config.formModel(model ->
                model.excludeProperties(EXC_FIELDS)
                        .excludeProperties(User.Fields.roles));
        config.beforeSave(ctx -> ctx.getDbBean().setRoles(USER_ROLES_CLIENT));

        config.formState((formState, model) -> {
            Long userId = formState.getBean().getId();
            if (userId != null) {
                listDevices(model, userId);
                addDeviceForm(model, userId);
                clientProfileForm(model, userId);
                listSessions(model, userId);
            }
        });
    }

    private void clientProfileForm(Model model, Long userId) {
        ClientProfile profile = clientProfileService.findOrCreate(userId);
        FormState<ClientProfile> formState = createFormState(profile, model, "clientProfileFormState");
        formState.getModel().useProperties(ClientProfile.Fields.sessionExpirationMinutes);
        formState.setAction(getMyLink(userId, "client-profile"));
    }

    private void addDeviceForm(Model model, Long userId) {
        FormState<AddDeviceRequest> deviceFormState = createFormState(
                new AddDeviceRequest(),
                model,
                "deviceFormState");
        deviceFormState.setAction(getMyLink(userId, "devices"));

        model.addAttribute("worksiteOptions", userWorksiteService.listOptions(userId));
        model.addAttribute("worksitesAction", getMyLink(userId, "worksites"));
    }

    private void listSessions(Model model, Long userId) {
        int recentCount = prefsService.getPrefs(GeoPreferences.class).getClientSessionRecentCount();
        var clientSessionsPageState = createPageState(CreatePageStateParams.builderFor(ClientSession.class)
                .model(model)
                .modelAttributeName("clientSessionsPageState")
                .content(clientSessionService.listRecentByUserId(userId, recentCount))
                .build());
        clientSessionsPageState.getModel()
                .useProperties(
                        ClientSession.Fields.device,
                        AbstractEntity.Fields.created,
                        ClientSession.Fields.start,
                        ClientSession.Fields.stop);
        clientSessionsPageState.getModel()
                .withProperty(ClientSession.Fields.device)
                .setLabel("Device")
                .setTextValueSupplier(session -> session.getDevice().getName());
        clientSessionsPageState.getModel()
                .withProperty(ClientSession.Fields.start)
                .setLabel("Started")
                .setTextValueSupplier(session ->
                        session.getStart() == null ? null : session.getStart().getRecordedAt());
        clientSessionsPageState.getModel()
                .withProperty(ClientSession.Fields.stop)
                .setLabel("Stopped")
                .setTextValueSupplier(session ->
                        session.getStop() == null ? null : session.getStop().getRecordedAt());
        clientSessionsPageState.doHideFilter().doHidePagination();
    }

    private void listDevices(Model model, Long userId) {
        var devicesPageState = createPageState(CreatePageStateParams.builderFor(Device.class)
                .model(model)
                .modelAttributeName("devicesPageState")
                .content(deviceService.listUserDevices(userId))
                .build());
        devicesPageState.getModel()
                .useProperties(
                        Device.Fields.name,
                        Device.Fields.verificationCode,
                        AbstractEntity.Fields.created,
                        Device.Fields.verifiedAt);
        devicesPageState.getModel()
                .setLinkSupplier(Device.Fields.name, device ->
                        getLink("/admin/device", device.getId(), LINK_EDIT));
        devicesPageState.doHideFilter().doHidePagination();
    }

    @Override
    protected EntityIO<User, Long, UserDetails> getEntityIO() {
        return userService;
    }

    @PostMapping("{userId}/devices")
    public String addDevice(
            @PathVariable Long userId,
            @ModelAttribute AddDeviceRequest request,
            RedirectAttributes redirectAttributes
    ) {
        runWithMessage(redirectAttributes, () -> {
            User user = userService.getUserById(userId);
            Device device = deviceService.create(request.getName(), user);
            return device.getVerificationCode();
        });

        return redirect(getMyLink(userId, LINK_EDIT));
    }

    @PostMapping("{userId}/worksites")
    public String saveWorksites(
            @PathVariable Long userId,
            @RequestParam(name = "worksiteIds", required = false) List<Long> worksiteIds,
            RedirectAttributes redirectAttributes
    ) {
        runWithMessage(redirectAttributes, () -> {
            userWorksiteService.saveUserWorksites(userId, worksiteIds);
            return "Worksites saved.";
        });

        return redirect(getEditLink(userId));
    }

    @PostMapping("{userId}/client-profile")
    public String saveClientProfile(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer sessionExpirationMinutes,
            RedirectAttributes redirectAttributes
    ) {
        runWithMessage(redirectAttributes, () -> {
            ClientProfile profile = clientProfileService.findOrCreate(userId);
            profile.setSessionExpirationMinutes(sessionExpirationMinutes);
            clientProfileService.save(profile);
            return "Client profile saved.";
        });

        return redirect(getEditLink(userId));
    }

    @Setter
    @Getter
    @Ui(label = "Add device")
    public static class AddDeviceRequest {

        @Size(max = 128)
        private String name;
    }
}
