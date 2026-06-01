package com.geolog.server.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jmaster.core.ui.annot.Ui;
import lombok.Data;

@Data
@Ui(label = "Geolog client preferences", icon = "phone")
public class GeoClientPreferences {

    @Min(1)
    @Max(100)
    @Ui(label = "Location update interval (sec)")
    private int locationUpdateInterval = 10;
}
