package br.com.gasoutapp.dto.room;

import java.util.List;

import br.com.gasoutapp.domain.room.Room;
import br.com.gasoutapp.dto.user.UserDTO;
import lombok.Data;

@Data
public class RoomDTO {
	private String id;
	private RoomNameDTO details;
	private Long gasSensorValue;
	private Long umiditySensorValue;
	private UserDTO user;
	private Boolean notificationOn;
	private Boolean alarmOn;
	private Boolean sprinklersOn;
	private List<Long> recentGasSensorValues;

	public RoomDTO() {
	}

	public RoomDTO(Room entity) {
		super();
		this.id = entity.getId();
		this.details = new RoomNameDTO(entity.getName());
		this.gasSensorValue = entity.getGasSensorValue();
		this.umiditySensorValue = entity.getUmiditySensorValue();
		this.notificationOn = entity.isNotificationOn();
		this.alarmOn = entity.isAlarmOn();
		this.sprinklersOn = entity.isSprinklersOn();
		this.recentGasSensorValues = entity.getRecentGasSensorValues();

		if (entity.getUserEmail() != null) {
			this.user = new UserDTO(entity.getUserEmail());
		}
	}
}