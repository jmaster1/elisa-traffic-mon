package com.geolog.server.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jmaster.core.ui.annot.Ui;
import lombok.Data;

@Data
@Ui(label = "Geolog preferences", icon = "server")
public class GeoPreferences {

    @Min(1)
    @Max(100)
    @Ui(label = "Number of entries to show in recent session list")
    private int clientSessionRecentCount = 10;

    @Min(1)
    @Max(100)
    @Ui(label = "Client session autoclose (minutes)")
    private Integer clientSessionExpirationMinutes = 600;

    @Min(1)
    @Max(3600)
    @Ui(label = "Client session autoclose job interval (seconds)")
    private Integer clientSessionExpirationJobIntervalSeconds = 60;
}
