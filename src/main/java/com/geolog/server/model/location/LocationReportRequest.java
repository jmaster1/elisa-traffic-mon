package com.geolog.server.model.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record LocationReportRequest(
        @NotBlank String deviceUuid,
        @NotNull Double latitude,
        @NotNull Double longitude,
        Double accuracy,
        @NotNull Instant recordedAt
) {
}
