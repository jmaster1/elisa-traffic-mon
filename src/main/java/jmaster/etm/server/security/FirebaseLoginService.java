package jmaster.etm.server.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jmaster.core.service.SecurityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FirebaseLoginService {

    private final Set<String> allowedEmails;

    public FirebaseLoginService(@Value("${etm.security.allowed-emails}") String allowedEmails) {
        this.allowedEmails = Stream.of(allowedEmails.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .map(email -> email.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public void login(String idToken) throws FirebaseAuthException {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("Firebase ID token is required");
        }

        FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
        Object firebaseClaim = token.getClaims().get("firebase");
        if (!(firebaseClaim instanceof java.util.Map<?, ?> firebase)
                || !"google.com".equals(firebase.get("sign_in_provider"))) {
            throw new IllegalArgumentException("Only Google sign-in is supported");
        }
        if (token.getEmail() == null || token.getEmail().isBlank() || !token.isEmailVerified()) {
            throw new IllegalArgumentException("Google account does not provide a verified email address");
        }
        if (!allowedEmails.contains(token.getEmail().toLowerCase(Locale.ROOT))) {
            throw new SecurityException("This Google account is not allowed");
        }

        SecurityService.setAuth(
                User.withUsername(token.getEmail())
                        .password("")
                        .authorities(EtmUserRole.admin)
                        .build()
        );
    }
}
