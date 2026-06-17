package jmaster.etm.server.model.snapshot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jmaster.core.model.AbstractEntity;
import jmaster.core.ui.annot.Ui;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;

@Entity
@Table(
	name = "consumption_snapshot",
	indexes = {
		@Index(name = "idx_phone_timestamp_id", columnList = "phone_nr,timestamp,id"),
		@Index(name = "idx_timestamp_phone_id", columnList = "timestamp,phone_nr,id")
	}
)
@Getter
@Setter
@FieldNameConstants
@Ui(label = "Consumption snapshots", icon = "database")
public class ConsumptionSnapshot extends AbstractEntity<Long> {
	
	@Column(name = "phone_nr")
	@Ui(label = "Phone")
	private Long phoneNr;
	
	@Column(name = "used_gb")
	@Ui(label = "Used GB")
	private Float usedGb;
	
	@Column(name = "timestamp")
	@Ui(label = "Timestamp")
	private Instant timestamp;
}
