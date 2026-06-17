package jmaster.etm.server.config;

import jakarta.annotation.PostConstruct;
import jmaster.core.i18n.BasicMessageSource;
import jmaster.etm.server.model.snapshot.FetchConfig;
import jmaster.system.SystemPrefs;
import jmaster.system.prefs.AdminPrefsController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

@Configuration
@EnableCaching
public class AppConfig {

    @Bean
    public MessageSource messageSource(ObjectProvider<ConversionService> csProvider) {
        return new BasicMessageSource() {
        };
    }

    @PostConstruct
    public void init() {
        AdminPrefsController.KNOWN_PREFS_TYPES = new Class[]{
                FetchConfig.class,
                SystemPrefs.class,
        };
    }
}
