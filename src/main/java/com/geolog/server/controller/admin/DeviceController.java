package com.geolog.server.controller.admin;

import com.geolog.server.controller.Mapping;
import com.geolog.server.model.device.Device;
import com.geolog.server.model.device.DeviceFilter;
import com.geolog.server.model.device.DeviceService;
import jmaster.core.controller.AbstractEntityController;
import jmaster.core.controller.EntityControllerConfig;
import jmaster.core.model.AbstractEntity;
import jmaster.core.model.AbstractVersionedEntity;
import jmaster.core.service.EntityIO;
import jmaster.core.ui.PageState;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(Mapping.Admin.DEVICE)
public class DeviceController extends AbstractEntityController<Long, Device, DeviceFilter, UserDetails> {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    protected void configure(EntityControllerConfig<Device, DeviceFilter> config) {
        config.listTemplate(getListTemplateSpecific());
        config.pageState(PageState::doHideFilter);
        config.pageModel(model -> {
            model.useProperties(
                    Device.Fields.user,
                    Device.Fields.name,
                    Device.Fields.verificationCode,
                    AbstractEntity.Fields.created,
                    Device.Fields.verifiedAt);
            model.withProperty(Device.Fields.user)
                    .setLabel("User")
                    .setTextValueSupplier(device -> device.getUser() == null ? null : device.getUser().getNameOrEmail());
            model.setLinkSupplier(Device.Fields.user, device ->
                    device.getUser() == null ? null : getLink("/admin/user", device.getUser().getId(), LINK_EDIT));
            model.setLinkSupplier(Device.Fields.name, getEditLinkSupplier());
        });

        config.editTemplate(getEditTemplateSpecific());
        config.formModel(model -> {
            model.useProperties(
                    AbstractEntity.Fields.id,
                    AbstractVersionedEntity.Fields.version,
                    Device.Fields.name,
                    Device.Fields.uuid,
                    Device.Fields.verificationCode,
                    Device.Fields.verifiedAt);
            model.disableInput(Device.Fields.uuid, Device.Fields.verificationCode, Device.Fields.verifiedAt);
        });
    }

    @Override
    protected EntityIO<Device, Long, UserDetails> getEntityIO() {
        return deviceService;
    }

    @PostMapping("{id}/generate-code")
    public String generateCode(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        deviceService.generateVerificationCode(id);
        redirectAttributes.addFlashAttribute(ATTR_INFO_MESSAGE, "Verification code generated");
        return redirect(getRequestMapping(), id, LINK_EDIT);
    }

    @Override
    protected Page<Device> listPage(DeviceFilter filter) {
        return deviceService.list(filter);
    }
}
