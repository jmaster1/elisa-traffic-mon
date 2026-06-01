package jmaster.etm.server.config;

import jmaster.etm.server.model.GeoIcon;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import jmaster.core.config.FreeMarkerSharedVariablesConfigurer;
import org.springframework.stereotype.Component;

@Component
public class GeoFreeMarkerConfig implements FreeMarkerSharedVariablesConfigurer {

    @Override
    public void configure(Configuration freemarkerConfiguration) throws TemplateModelException {
        BeansWrapperBuilder builder = new BeansWrapperBuilder(Configuration.VERSION_2_3_34);
        TemplateHashModel staticModels = builder.build().getStaticModels();
        TemplateModel geoIconStatics = staticModels.get(GeoIcon.class.getName());
        freemarkerConfiguration.setSharedVariable(GeoIcon.class.getSimpleName(), geoIconStatics);
    }
}
