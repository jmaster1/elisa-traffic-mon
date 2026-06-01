package com.geolog.server.controller.client;

import com.geolog.server.model.device.Device;
import com.geolog.server.model.device.DeviceService;
import com.geolog.server.model.session.ClientSession;
import com.geolog.server.model.userworksite.UserWorksiteService;
import com.geolog.server.model.worksite.Worksite;
import com.geolog.server.security.ClientPrincipal;
import com.geolog.server.model.session.ClientSessionRequest;
import com.geolog.server.model.session.ClientSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jmaster.core.controller.AbstractController;
import jmaster.system.user.User;
import jmaster.system.user.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
public class ClientController extends AbstractController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    private final DeviceService deviceService;

    private final ClientSessionService sessionService;

    private final UserWorksiteService userWorksiteService;

    @GetMapping({"/client", "/client/"})
    public String client(@AuthenticationPrincipal ClientPrincipal principal) {
        return principal != null ? redirect("/client/status") : "client/device-auth";
    }

    @GetMapping("/client/status")
    public String status(@AuthenticationPrincipal ClientPrincipal principal, Model model) {
        Device device = resolveDevice(principal);
        if (device == null) {
            return redirect("/client");
        }

        return sessionService.findActive(device)
                .map(session -> {
                    Worksite activeWorksite = session.getWorksite();
                    model.addAttribute("worksitesJson", toJson(activeWorksite == null ? List.of() : List.of(activeWorksite)));
                    Instant started = session.getStart().getRecordedAt();
                    model.addAttribute("startedAt", started);
                    model.addAttribute("startedAtIso", started.toString());
                    model.addAttribute("startedAtText", TIME_FORMATTER.format(atZone(started)));
                    if (activeWorksite != null) {
                        model.addAttribute("activeWorksiteId", activeWorksite.getId());
                        model.addAttribute("activeWorksiteName", activeWorksite.getName());
                    }
                    return "client/status-started";
                })
                .orElseGet(() -> {
                    List<Worksite> worksites = userWorksiteService.listAssignedWorksites(principal.userId());
                    model.addAttribute("worksitesJson", toJson(worksites));
                    return "client/status-idle";
                });
    }

    @GetMapping("/client/session-history")
    public String sessionHistory(@AuthenticationPrincipal ClientPrincipal principal, Model model) {
        if (principal == null) {
            throw new NoSuchElementException("Device not found");
        }

        List<SessionHistoryRow> rows = sessionService.listRecentByUserId(principal.userId(), 10).stream()
                .map(this::toSessionHistoryRow)
                .toList();
        model.addAttribute("sessionHistoryRows", rows);
        return "client/session-history";
    }

    @GetMapping("/client/signup")
    public String signupForm() {
        return "client/signup";
    }

    @GetMapping("/client/debug")
    public String debug() {
        return "client/debug";
    }

    @PostMapping("/client/deviceAuth")
    public String deviceAuth(
            @RequestParam String deviceUuid,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Device device = deviceService.findByUuid(deviceUuid.trim()).orElse(null);
        if (device == null) {
            return "redirect:/client/signup";
        }

        authenticateClient(device, request, response);
        return "redirect:/client/status";
    }

    @PostMapping("/client/signup")
    public String signup(
            @RequestParam String verificationCode,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        try {
            Device device = deviceService.signup(verificationCode);
            authenticateClient(device, request, response);
            model.addAttribute("deviceUuid", device.getUuid());
            return "client/save-device";
        } catch (NoSuchElementException error) {
            model.addAttribute("verificationCode", verificationCode);
            model.addAttribute("signupError", "Invalid verification code");
            return "client/signup";
        }
    }

    @PostMapping("/client/start")
    public String start(
            @Valid ClientSessionRequest request,
            @AuthenticationPrincipal ClientPrincipal principal
    ) {
        Device device = requireDevice(principal);
        sessionService.start(device, request);
        return "redirect:/client/status";
    }

    @PostMapping("/client/stop")
    public String stop(
            @Valid ClientSessionRequest request,
            @AuthenticationPrincipal ClientPrincipal principal
    ) {
        Device device = requireDevice(principal);
        sessionService.stop(device, request);
        return "redirect:/client/status";
    }

    private void authenticateClient(Device device, HttpServletRequest request, HttpServletResponse response) {
        User user = device.getUser();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                new ClientPrincipal(
                        device.getId(),
                        device.getUuid(),
                        device.getName(),
                        user.getId(),
                        user.getNameOrEmail()
                ),
                null,
                List.of(UserRole.client)
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        securityContextRepository.saveContext(context, request, response);
    }

    private Device requireDevice(ClientPrincipal principal) {
        Device device = resolveDevice(principal);
        if (device == null) {
            throw new NoSuchElementException("Device not found");
        }

        return device;
    }

    private Device resolveDevice(ClientPrincipal principal) {
        return principal == null ? null :
            deviceService.findByUuid(principal.deviceUuid()).orElse(null);
    }

    private SessionHistoryRow toSessionHistoryRow(ClientSession session) {
        Instant started = session.getStart() == null ? null : session.getStart().getRecordedAt();
        Instant stopped = session.getStop() == null ? null : session.getStop().getRecordedAt();
        String date = started == null ? "" : DateTimeFormatter.ISO_LOCAL_DATE.format(atZone(started));
        String time = started == null ? "" : TIME_FORMATTER.format(atZone(started));
        String duration = started == null || stopped == null ? "" : formatDuration(Duration.between(started, stopped));
        return new SessionHistoryRow(date, time, duration);
    }

    private String formatDuration(Duration duration) {
        long minutes = Math.max(duration.toMinutes(), 0);
        return "%02d:%02d".formatted(minutes / 60, minutes % 60);
    }

    @Getter
    @RequiredArgsConstructor
    public static class SessionHistoryRow {
        private final String date;
        private final String time;
        private final String duration;
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Void> handleNotFound() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<String> handleInvalidSessionState(IllegalStateException error) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error.getMessage());
    }
}
