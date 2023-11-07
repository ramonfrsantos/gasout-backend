package br.com.gasoutapp.domain.entity.room;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import br.com.gasoutapp.domain.entity.enums.SensorTypeEnum;
import lombok.Data;

@Entity
@Data
@Audited(withModifiedFlag = true)
@AuditTable(value = "aud_t_sensor", catalog = "audit")
@Table(name = "t_sensor")
public class Sensor {
	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id")
	private String id;

	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	private SensorTypeEnum sensorType;
	
	@Column(name = "timestamp")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	@Column(name = "value")
	private Long sensorValue;
	
	@ManyToOne
	@JoinColumn(name = "fk_room")
	private Room room;
}