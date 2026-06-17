package jmaster.etm.server.model.snapshot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jmaster.core.model.AbstractEntity;
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
public class ConsumptionSnapshot extends AbstractEntity<Long> {
	
	@Column(name = "phone_nr")
	private Long phoneNr;
	
	@Column(name = "used_gb")
	private Float usedGb;
	
	@Column(name = "timestamp")
	private Instant timestamp;
}
