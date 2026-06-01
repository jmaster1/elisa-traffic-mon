package jmaster.etm.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jmaster.core.model.AbstractEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.Date;

@Entity
@Table(name = "consumption_snapshot")
@Getter
@Setter
@FieldNameConstants
public class ConsumptionSnapshot extends AbstractEntity<Long> {
	
	@Column(name = "phone_nr")
	private Long phoneNr;
	
	@Column(name = "used_gb")
	private Float usedGb;
	
	@Column(name = "timestamp")
	private Date timestamp;
}
