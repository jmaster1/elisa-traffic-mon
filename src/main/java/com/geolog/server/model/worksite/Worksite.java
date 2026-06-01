package com.geolog.server.model.worksite;

import com.geolog.server.model.GeoIcon;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jmaster.core.model.AbstractVersionedEntity;
import jmaster.core.ui.annot.Ui;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(name = "geo_worksite")
@Getter
@Setter
@FieldNameConstants
@Ui(icon = GeoIcon.WORKSITE)
public class Worksite extends AbstractVersionedEntity<Long> {

    @Column(nullable = false, length = 128, unique = true)
    @Ui(label = "Name")
    private String name;

    @Column
    @Ui(label = "Latitude")
    private Double latitude;

    @Column
    @Ui(label = "Longitude")
    private Double longitude;

    @Column
    @Min(value = 1)
    @Max(value = 100000)
    @Ui(label = "Zone radius (m)")
    private Integer radiusMeters;

    @Override
    public String getOptionText() {
        return name;
    }
}
