package jmaster.etm.server.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FirebaseConfig {

    private final ResourceLoader resourceLoader;

    @Value("${etm.firebase.credentials.location:${firebase.credentials.location:}}")
    private String credentialsLocation;

    @PostConstruct
    public void init() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        Resource resource = getCredentialsResource();
        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized from {}", resource.getDescription());
        }
    }

    private Resource getCredentialsResource() {
        if (credentialsLocation == null || credentialsLocation.isBlank()) {
            Resource defaultResource = getDefaultCredentialsResource();
            if (defaultResource.exists()) {
                return defaultResource;
            }
            throw new IllegalStateException(
                    "Firebase credentials are not configured. Set etm.firebase.credentials.location, firebase.credentials.location, "
                            + "ETM_FIREBASE_CREDENTIALS_LOCATION, or put credentials into "
                            + getDefaultCredentialsPath() + ".");
        }
        Resource resource = resourceLoader.getResource(credentialsLocation);
        if (!resource.exists()) {
            throw new IllegalStateException("Firebase credentials not found: " + credentialsLocation);
        }
        return resource;
    }

    private Resource getDefaultCredentialsResource() {
        return resourceLoader.getResource(getDefaultCredentialsPath().toUri().toString());
    }

    private Path getDefaultCredentialsPath() {
        return Path.of(System.getProperty("user.home"), "firebase-adminsdk.json");
    }
}
