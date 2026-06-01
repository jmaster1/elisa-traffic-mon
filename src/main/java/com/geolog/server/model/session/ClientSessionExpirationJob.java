package com.geolog.server.model.session;

import com.geolog.server.config.GeoPreferences;
import jmaster.system.prefs.PrefsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientSessionExpirationJob {

    private final ClientSessionService sessionService;
    private final PrefsService prefsService;

    public void closeExpiredSessions() {
        Integer minutes = prefsService.getPrefs(GeoPreferences.class).getClientSessionExpirationMinutes();
        int closed = sessionService.closeExpired(minutes);
        if (closed > 0) {
            log.info("Client session expiration job closed {} expired session(s), defaultExpiration={} minute(s)", closed, minutes);
        } else {
            log.debug("Client session expiration job found no expired sessions, defaultExpiration={} minute(s)", minutes);
        }
    }
}
