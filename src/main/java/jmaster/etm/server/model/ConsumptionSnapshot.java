package jmaster.etm.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

import java.util.Date;

@Entity
@Table(name = "consumption_snapshot")
@FieldNameConstants
public class ConsumptionSnapshot {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@Column(name = "phone_nr")
	private Long phoneNr;
	
	@Column(name = "used_gb")
	private Float usedGb;
	
	@Column(name = "timestamp")
	private Date timestamp;
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public Long getId()
	{
		return id;
	}
	
	public Long getPhoneNr()
	{
		return phoneNr;
	}
	
	public void setPhoneNr(Long phoneNr)
	{
		this.phoneNr = phoneNr;
	}
	
	public Float getUsedGb()
	{
		return usedGb;
	}
	
	public void setUsedGb(Float usedGb)
	{
		this.usedGb = usedGb;
	}
	
	public Date getTimestamp()
	{
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}
}
