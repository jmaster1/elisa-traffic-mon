package com.geolog.server.model.device;

import com.geolog.server.model.GeoIcon;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jmaster.core.model.AbstractVersionedEntity;
import jmaster.core.ui.annot.Ui;
import jmaster.system.user.User;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

@Entity
@Table(
        name = "geo_device",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_geo_device_user_name",
                columnNames = {"user_id", "name"}
        )
)
@Getter
@Setter
@FieldNameConstants
@Ui(icon = GeoIcon.DEVICE)
public class Device extends AbstractVersionedEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Ui(label = "User")
    private User user;

    @Column(name = "uuid", nullable = false, unique = true, length = 128)
    @Ui(label = "UUID")
    private String uuid;

    @Column(name = "name", nullable = false, length = 128)
    @Ui(label = "Name")
    private String name;

    @Column(name = "verification_code", unique = true, length = 6)
    @Ui(label = "Verification code")
    private String verificationCode;

    @Column
    @Ui(label = "Verification at")
    private Instant verifiedAt;
}
