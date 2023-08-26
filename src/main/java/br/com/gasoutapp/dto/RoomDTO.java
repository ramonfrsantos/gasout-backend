package br.com.gasoutapp.dto;

import br.com.gasoutapp.domain.Room;
import br.com.gasoutapp.domain.enums.RoomNameEnum;
import lombok.Data;

@Data
public class RoomDTO {
	private String id;
	private RoomNameEnum name;
	private Long sensorValue;
	private UserDTO user;
	private Boolean notificationOn;
	private Boolean alarmOn;
	private Boolean sprinklersOn;

	public RoomDTO() {
	}

	public RoomDTO(Room entity) {
		super();
		this.id = entity.getId();
		this.name = entity.getName();
		this.sensorValue = entity.getSensorValue();
		this.notificationOn = entity.isNotificationOn();
		this.alarmOn = entity.isAlarmOn();
		this.sprinklersOn = entity.isSprinklersOn();

		if (entity.getUserEmail() != null) {
			this.user = new UserDTO(entity.getUserEmail());
		}
	}
}