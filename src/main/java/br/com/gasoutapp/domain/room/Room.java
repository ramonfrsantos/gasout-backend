package br.com.gasoutapp.domain.room;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import br.com.gasoutapp.domain.enums.RoomNameEnum;
import lombok.Data;

@DynamicUpdate
@Entity
@Data
@Audited(withModifiedFlag = true)
@AuditTable(value = "aud_t_room", catalog = "audit")
@Table(name = "t_room")
@Where(clause = "deleted = false")
public class Room {
	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id")
	private String id;

	@Column(name = "name")
	@Enumerated(EnumType.STRING)
	private RoomNameEnum name;

	@Column(name = "notification_on")
	private boolean notificationOn;

	@Column(name = "alarm_on")
	private boolean alarmOn;

	@Column(name = "sprinklers_on")
	private boolean sprinklersOn;

	@Column(name = "gas_sensor_value")
	private Long gasSensorValue;
	
	@Column(name = "umidity_sensor_value")
	private Long umiditySensorValue;

	@Column(name = "user_email")
	private String userEmail;

	@Column(name = "deleted")
	private boolean deleted;
}