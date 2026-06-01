package com.geolog.server.model.device;

import jmaster.core.model.AbstractEntity;
import jmaster.core.model.LocalDateTimeRange;
import jmaster.core.model.filter.DefaultFilter;
import jmaster.core.util.jpa.SpecBuilder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeviceFilter extends DefaultFilter<Device> {

    private String search;

    private Long userId;

    private final LocalDateTimeRange createdRange = new LocalDateTimeRange(AbstractEntity.Fields.created);

    @Override
    protected void apply(SpecBuilder<Device> spec) {
        spec.likeAny(search, Device.Fields.name, Device.Fields.uuid, Device.Fields.verificationCode);
        spec.eq(userId, Device.Fields.user, AbstractEntity.Fields.id);
        createdRange.apply(spec);
    }
}
