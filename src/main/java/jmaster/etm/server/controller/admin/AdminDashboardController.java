package jmaster.etm.server.controller.admin;

import jmaster.system.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserRepository userRepository;

    @GetMapping({"/admin", "/admin/"})
    public String dashboard(Model model) {
        return "admin/dashboard";
    }
}
