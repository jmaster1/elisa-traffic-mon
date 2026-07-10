package jmaster.etm.server.config;

import jmaster.core.i18n.BasicMessageSource;
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

}
