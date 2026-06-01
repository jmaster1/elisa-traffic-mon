package jmaster.etm.server.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jmaster.core.controller.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminLoginController {

    @GetMapping("/admin/login")
    public String login(HttpServletRequest request, Model model) {
        moveMessage(request, model, AbstractController.ATTR_INFO_MESSAGE);
        moveMessage(request, model, AbstractController.ATTR_ERROR_MESSAGE);
        return "admin/login";
    }

    @GetMapping("/login")
    public String defaultLogin() {
        return "redirect:/admin/login";
    }

    private void moveMessage(HttpServletRequest request, Model model, String attribute) {
        Object message = request.getSession().getAttribute(attribute);
        if (message == null) {
            return;
        }

        model.addAttribute(attribute, message);
        request.getSession().removeAttribute(attribute);
    }
}
