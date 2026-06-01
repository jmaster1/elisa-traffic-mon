package com.geolog.server.model.clientprofile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jmaster.core.model.AbstractVersionedEntity;
import jmaster.core.ui.annot.Ui;
import jmaster.system.user.User;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(name = "geo_client_profile")
@Getter
@Setter
@FieldNameConstants
@Ui(label = "Client profile", icon = "person-gear")
public class ClientProfile extends AbstractVersionedEntity<Long> {

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id")
    @Ui(label = "User")
    private User user;

    @Column
    @Min(1)
    @Max(10080)
    @Ui(label = "Session autoclose (minutes)")
    private Integer sessionExpirationMinutes;
}
