package com.geolog.server.model.session;

import com.geolog.server.model.device.Device;
import jmaster.core.model.AbstractEntity;
import jmaster.core.model.LocalDateTimeRange;
import jmaster.core.model.filter.DefaultFilter;
import jmaster.core.ui.annot.UiExclude;
import jmaster.core.util.jpa.SpecBuilder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ClientSessionFilter extends DefaultFilter<ClientSession> {

    @UiExclude
    private Long deviceId;

    @UiExclude
    private Long userId;

    @UiExclude
    private Long worksiteId;

    private String search;

    private Boolean active;

    private StopReason stopReason;

    private final LocalDateTimeRange createdRange = new LocalDateTimeRange(AbstractEntity.Fields.created);

    @Override
    protected void apply(SpecBuilder<ClientSession> spec) {
        spec.eq(deviceId, ClientSession.Fields.device, AbstractEntity.Fields.id);
        spec.eq(userId, ClientSession.Fields.device, Device.Fields.user, AbstractEntity.Fields.id);
        spec.eq(worksiteId, ClientSession.Fields.worksite, AbstractEntity.Fields.id);
        spec.eq(stopReason, ClientSession.Fields.stopReason);
        spec.isNull(active, ClientSession.Fields.stop, LocationData.Fields.recordedAt);
        createdRange.apply(spec);
        spec.likeAny(search,
                ClientSession.PP_USER_NAME,
                ClientSession.PP_DEVICE_NAME,
                ClientSession.PP_WORKSITE_NAME);
    }
}
