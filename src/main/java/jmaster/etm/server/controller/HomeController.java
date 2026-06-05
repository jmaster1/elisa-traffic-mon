package jmaster.etm.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/consumption/report";
    }

    @GetMapping("/favicon.ico")
    public String favicon() {
        return "redirect:/static/favicon.svg";
    }
}
