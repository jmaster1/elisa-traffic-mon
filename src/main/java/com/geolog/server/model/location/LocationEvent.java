package com.geolog.server.model.location;

import com.geolog.server.model.device.Device;
import com.geolog.server.model.session.LocationData;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jmaster.core.model.AbstractVersionedEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(name = "geo_location_events")
@Getter
@Setter
@FieldNameConstants
public class LocationEvent extends AbstractVersionedEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Embedded
    private LocationData location;
}
