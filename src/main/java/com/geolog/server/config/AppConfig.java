package com.geolog.server.config;

import jakarta.annotation.PostConstruct;
import jmaster.core.i18n.BasicMessageSource;
import jmaster.system.SystemPrefs;
import jmaster.system.prefs.AdminPrefsController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@Configuration
@EnableCaching
@EnableJdbcHttpSession(tableName = "SPRING_SESSION", maxInactiveIntervalInSeconds = 86400)
public class AppConfig {

    @Bean
    public MessageSource messageSource(ObjectProvider<ConversionService> csProvider) {
        return new BasicMessageSource() {
        };
    }

    @PostConstruct
    public void init() {
        AdminPrefsController.KNOWN_PREFS_TYPES = new Class[]{
                GeoPreferences.class,
                GeoClientPreferences.class,
                SystemPrefs.class,
        };
    }
}
