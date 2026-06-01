package com.geolog.server.model.session;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ClientSessionRequest(
        @NotNull Double latitude,
        @NotNull Double longitude,
        Double accuracy,
        @NotNull Instant recordedAt
) {
}
