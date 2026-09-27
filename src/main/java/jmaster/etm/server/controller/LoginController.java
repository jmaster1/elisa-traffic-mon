package jmaster.etm.server.controller;

import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.http.HttpSession;
import jmaster.core.security.LoginRedirectEntryPoint;
import jmaster.etm.server.security.FirebaseLoginService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Controller
public class LoginController {

    private final FirebaseLoginService firebaseLoginService;

    @Value("${firebase.web.api-key}")
    private String firebaseApiKey;

    @Value("${firebase.web.auth-domain}")
    private String firebaseAuthDomain;

    @Value("${firebase.web.project-id}")
    private String firebaseProjectId;

    @Value("${firebase.web.storage-bucket}")
    private String firebaseStorageBucket;

    @Value("${firebase.web.messaging-sender-id}")
    private String firebaseMessagingSenderId;

    @Value("${firebase.web.app-id}")
    private String firebaseAppId;

    public LoginController(FirebaseLoginService firebaseLoginService) {
        this.firebaseLoginService = firebaseLoginService;
    }

    @GetMapping("/login")
    public String login(Authentication authentication, Model model) {
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserDetails) {
            return "redirect:/";
        }

        model.addAttribute("firebaseApiKey", firebaseApiKey);
        model.addAttribute("firebaseAuthDomain", firebaseAuthDomain);
        model.addAttribute("firebaseProjectId", firebaseProjectId);
        model.addAttribute("firebaseStorageBucket", firebaseStorageBucket);
        model.addAttribute("firebaseMessagingSenderId", firebaseMessagingSenderId);
        model.addAttribute("firebaseAppId", firebaseAppId);
        return "login";
    }

    @PostMapping("/firebase-login")
    public ResponseEntity<?> firebaseLogin(
            @RequestBody Map<String, String> payload,
            HttpSession session) {
        try {
            firebaseLoginService.login(payload.get("idToken"));
            String redirectUrl = LoginRedirectEntryPoint.consumeLoginRedirectUrl(session, "/consumption/report");
            return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));
        } catch (FirebaseAuthException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Google authentication failed"));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "This Google account is not allowed"));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            String message = ex.getMessage() == null
                    ? "Google authentication failed"
                    : ex.getMessage();
            return ResponseEntity.badRequest().body(Map.of("message", message));
        }
    }
}
