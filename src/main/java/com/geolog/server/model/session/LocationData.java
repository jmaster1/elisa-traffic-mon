package com.geolog.server.model.session;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class LocationData {

    @Column(name = "recorded_at")
    private Instant recordedAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "accuracy")
    private Double accuracy;

    public static LocationData from(ClientSessionRequest request) {
        return new LocationData(
                request.recordedAt(),
                Instant.now(),
                request.latitude(),
                request.longitude(),
                request.accuracy()
        );
    }
}
