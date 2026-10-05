package jmaster.etm.server.controller;

import jmaster.etm.server.model.snapshot.TeliaPlaywrightSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class TeliaLoginController {

    private final TeliaPlaywrightSessionService teliaPlaywrightSessionService;

    @GetMapping("/telia/login")
    String teliaLogin() {
        return "telia/login";
    }

    @PostMapping("/telia/login/start")
    @ResponseBody
    TeliaPlaywrightSessionService.SessionStatus startTeliaLogin() {
        teliaPlaywrightSessionService.startTeliaSessionAsync();
        return teliaPlaywrightSessionService.getSessionStatus();
    }

    @GetMapping("/telia/login/status")
    @ResponseBody
    TeliaPlaywrightSessionService.SessionStatus teliaLoginStatus() {
        return teliaPlaywrightSessionService.getSessionStatus();
    }
}
