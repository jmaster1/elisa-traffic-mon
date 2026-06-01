package com.geolog.server.model.userworksite;

import com.geolog.server.model.GeoIcon;
import com.geolog.server.model.worksite.Worksite;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jmaster.core.model.AbstractEntity;
import jmaster.core.ui.annot.Ui;
import jmaster.system.user.User;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(
        name = "geo_user_worksite",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_geo_user_worksite_user_worksite",
                columnNames = {"user_id", "worksite_id"}
        )
)
@Getter
@Setter
@FieldNameConstants
@Ui(label = "User-Worksite", icon = GeoIcon.USER_WORKSITE)
public class UserWorksite extends AbstractEntity<Long> {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "worksite_id", nullable = false)
    private Worksite worksite;
}
