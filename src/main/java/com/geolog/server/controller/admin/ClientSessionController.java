package com.geolog.server.controller.admin;

import com.geolog.server.controller.Mapping;
import com.geolog.server.model.session.ClientSession;
import com.geolog.server.model.session.ClientSessionFilter;
import com.geolog.server.model.session.ClientSessionService;
import jmaster.core.controller.AbstractEntityController;
import jmaster.core.controller.EntityControllerConfig;
import jmaster.core.model.AbstractEntity;
import jmaster.core.model.AbstractVersionedEntity;
import jmaster.core.model.filter.DefaultFilter;
import jmaster.core.service.EntityIO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/clientsession")
@RequiredArgsConstructor
public class ClientSessionController extends AbstractEntityController<Long, ClientSession, ClientSessionFilter, UserDetails> {

    private static final String STARTED = "started";
    private static final String STOPPED = "stopped";

    private final ClientSessionService clientSessionService;

    @Override
    protected void configure(EntityControllerConfig<ClientSession, ClientSessionFilter> config) {
        config.filterDefaultSort(AbstractEntity.Fields.created, DefaultFilter.SORT_DESC);
        config.filter(filter ->
                filter.fetch(ClientSession.PP_USER.getPath(), ClientSession.Fields.worksite));
        config.pageModel(model -> {
            model.clearProperties();

            model.addProperty(ClientSession.PP_USER_NAME).setLabel("User")
                .setLinkSupplier(session -> getLinkEdit(
                        Mapping.Admin.USER, session.getDevice().getUser().getId()));

            model.addProperty(ClientSession.PP_DEVICE_NAME).setLabel("Device")
                    .setLinkSupplier(session -> getLinkEdit(
                            Mapping.Admin.DEVICE, session.getDevice().getId()));

            model.addProperty(ClientSession.PP_WORKSITE_NAME).setLabel("Worksite")
                    .setLinkSupplier(session -> session.getWorksite() == null ? null :
                            getLinkEdit(Mapping.Admin.WORKSITE, session.getWorksite().getId()));

            model.addProperty(ClientSession.PP_STARTED).setLabel("Started");
            model.addProperty(ClientSession.PP_STOPPED).setLabel("Stopped");
            model.addProperty(ClientSession.Fields.stopReason);
        });

        config.formModel(model -> {
            model.useProperties(
                    AbstractEntity.Fields.id,
                    AbstractVersionedEntity.Fields.version,
                    ClientSession.Fields.device,
                    ClientSession.Fields.worksite,
                    ClientSession.Fields.start,
                    ClientSession.Fields.stop,
                    ClientSession.Fields.stopReason);
            model.disableInput(
                    ClientSession.Fields.device,
                    ClientSession.Fields.worksite,
                    ClientSession.Fields.start,
                    ClientSession.Fields.stop,
                    ClientSession.Fields.stopReason);
        });
    }

    @Override
    protected EntityIO<ClientSession, Long, UserDetails> getEntityIO() {
        return clientSessionService;
    }
}
