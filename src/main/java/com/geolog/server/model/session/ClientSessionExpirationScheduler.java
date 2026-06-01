package com.geolog.server.model.session;

import com.geolog.server.config.GeoPreferences;
import jmaster.system.prefs.PrefsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ClientSessionExpirationScheduler implements SchedulingConfigurer {

    private final ClientSessionExpirationJob job;
    private final PrefsService prefsService;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(job::closeExpiredSessions, triggerContext -> {
            Instant lastCompletion = triggerContext.lastCompletion();
            Instant base = lastCompletion == null ? Instant.now() : lastCompletion;
            return base.plus(resolveInterval());
        });
    }

    private Duration resolveInterval() {
        Integer seconds = prefsService.getPrefs(GeoPreferences.class).getClientSessionExpirationJobIntervalSeconds();
        return Duration.ofSeconds(seconds == null || seconds <= 0 ? 60 : seconds);
    }
}
