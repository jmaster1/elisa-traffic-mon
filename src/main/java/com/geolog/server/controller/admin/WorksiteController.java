package com.geolog.server.controller.admin;

import com.geolog.server.controller.Mapping;
import com.geolog.server.model.worksite.Worksite;
import com.geolog.server.model.worksite.WorksiteFilter;
import com.geolog.server.model.worksite.WorksiteService;
import jmaster.core.controller.AbstractEntityController;
import jmaster.core.controller.EntityControllerConfig;
import jmaster.core.model.AbstractVersionedEntity;
import jmaster.core.service.EntityIO;
import jmaster.core.ui.PageState;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(Mapping.Admin.WORKSITE)
@RequiredArgsConstructor
public class WorksiteController extends AbstractEntityController<Long, Worksite, WorksiteFilter, UserDetails> {

    private final WorksiteService worksiteService;

    @Override
    protected void configure(EntityControllerConfig<Worksite, WorksiteFilter> config) {
        config.listTemplate(getListTemplateSpecific());
        config.pageState(PageState::doHideFilter);
        config.pageModel(model ->
                model.excludeProperties(AbstractVersionedEntity.FIELDS_ALL_BUT_CREATED)
                .setLinkSupplier(Worksite.Fields.name, getEditLinkSupplier()));

        config.editTemplate(getEditTemplateSpecific());
    }

    @Override
    protected EntityIO<Worksite, Long, UserDetails> getEntityIO() {
        return worksiteService;
    }
}
