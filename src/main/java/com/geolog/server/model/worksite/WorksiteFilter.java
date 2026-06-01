package com.geolog.server.model.worksite;

import jmaster.core.model.filter.DefaultFilter;
import jmaster.core.util.jpa.SpecBuilder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WorksiteFilter extends DefaultFilter<Worksite> {

    private String search;

    @Override
    protected void apply(SpecBuilder<Worksite> spec) {
        spec.likeAny(search, Worksite.Fields.name);
    }
}
