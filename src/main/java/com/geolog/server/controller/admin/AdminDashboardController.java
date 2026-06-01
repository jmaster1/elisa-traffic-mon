package com.geolog.server.controller.admin;

import com.geolog.server.model.device.DeviceRepository;
import com.geolog.server.model.session.ClientSessionRepository;
import com.geolog.server.model.worksite.WorksiteRepository;
import jmaster.system.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final WorksiteRepository worksiteRepository;
    private final ClientSessionRepository clientSessionRepository;

    @GetMapping({"/admin", "/admin/"})
    public String dashboard(Model model) {
        long devices = deviceRepository.count();

        model.addAttribute("usersCount", userRepository.count());
        model.addAttribute("devicesCount", devices);
        model.addAttribute("verifiedDevicesCount", deviceRepository.countByVerifiedAtIsNotNull());
        model.addAttribute("worksitesCount", worksiteRepository.count());
        model.addAttribute("activeSessionsCount", clientSessionRepository.countActive());

        return "admin/dashboard";
    }
}
