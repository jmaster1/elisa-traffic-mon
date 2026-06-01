package com.geolog.server.controller.admin;

import com.geolog.server.model.userworksite.UserWorksite;
import com.geolog.server.model.userworksite.UserWorksiteFilter;
import com.geolog.server.model.userworksite.UserWorksiteService;
import jmaster.core.controller.AbstractEntityController;
import jmaster.core.controller.EntityControllerConfig;
import jmaster.core.service.EntityIO;
import jmaster.core.ui.PageState;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/userworksite")
@RequiredArgsConstructor
public class UserWorksiteController extends AbstractEntityController<Long, UserWorksite, UserWorksiteFilter, UserDetails> {

    private final UserWorksiteService userWorksiteService;

    @Override
    protected void configure(EntityControllerConfig<UserWorksite, UserWorksiteFilter> config) {
        config.listTemplate(getListTemplateSpecific());
        config.pageState(PageState::doHideFilter).pageState(
                (ps, model) -> {
                    addMatrixAttributes(model);
        });
        config.editTemplate(getEditTemplateSpecific());
    }

    private void addMatrixAttributes(Model model) {
        model.addAttribute("matrixUsers", userWorksiteService.listMatrixUsers());
        model.addAttribute("matrixWorksites", userWorksiteService.listMatrixWorksites());
        model.addAttribute("matrixAssignedKeys", userWorksiteService.listMatrixAssignedKeys());
        model.addAttribute("matrixAction", getMyLink("matrix"));
        model.addAttribute("matrixTableClass", String.join(" ", PageState.DEFAULT_TABLE_CLASSES));
    }

    @Override
    protected EntityIO<UserWorksite, Long, UserDetails> getEntityIO() {
        return userWorksiteService;
    }

    @PostMapping("matrix")
    public String saveMatrix(
            @RequestParam(name = "assignments", required = false) List<String> assignments,
            RedirectAttributes redirectAttributes
    ) {
        runWithMessage(redirectAttributes, () -> {
            userWorksiteService.saveMatrix(assignments);
            return "User worksites saved.";
        });

        return redirect(getRequestMapping(), LINK_LIST);
    }
}
