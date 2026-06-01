package com.geolog.server.model.session;

import com.geolog.server.model.GeoIcon;
import com.geolog.server.model.device.Device;
import com.geolog.server.model.worksite.Worksite;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jmaster.core.model.AbstractVersionedEntity;
import jmaster.core.model.PropertyPath;
import jmaster.core.ui.annot.Ui;
import jmaster.system.user.User;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.EmbeddedColumnNaming;

@Entity
@Table(name = "geo_client_sessions")
@Getter
@Setter
@FieldNameConstants
@Ui(label = "Session", icon = GeoIcon.CLIENT_SESSION)
public class ClientSession extends AbstractVersionedEntity<Long> {

    public static final PropertyPath PP_USER = new PropertyPath(Fields.device, Device.Fields.user);
    public static final PropertyPath PP_USER_NAME = new PropertyPath(PP_USER, User.Fields.name);
    public static final PropertyPath PP_DEVICE_NAME = new PropertyPath(Fields.device, Device.Fields.name);
    public static final PropertyPath PP_WORKSITE_NAME = new PropertyPath(Fields.worksite, Worksite.Fields.name);
    public static final PropertyPath PP_STARTED = new PropertyPath(Fields.start, LocationData.Fields.receivedAt);
    public static final PropertyPath PP_STOPPED = new PropertyPath(Fields.stop, LocationData.Fields.receivedAt);

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worksite_id")
    @Ui(label = "Worksite")
    private Worksite worksite;

    @Embedded
    @EmbeddedColumnNaming("start_%s")
    @Ui(label = "Start")
    private LocationData start;

    @Embedded
    @EmbeddedColumnNaming("stop_%s")
    @Ui(label = "Stop")
    private LocationData stop;

    @Enumerated(EnumType.STRING)
    @Column()
    @Ui(label = "Stop reason")
    private StopReason stopReason;
}
