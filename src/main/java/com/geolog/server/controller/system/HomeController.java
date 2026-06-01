package com.geolog.server.controller.system;

import jmaster.system.user.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String welcome(Authentication authentication) {
        if (hasRole(authentication, UserRole.admin)) {
            return "redirect:/admin";
        }
        if (hasRole(authentication, UserRole.client)) {
            return "redirect:/client";
        }

        return "welcome";
    }

    private boolean hasRole(Authentication authentication, UserRole role) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String authorityName = "ROLE_" + role.name();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authorityName.equals(authority.getAuthority()));
    }
}
