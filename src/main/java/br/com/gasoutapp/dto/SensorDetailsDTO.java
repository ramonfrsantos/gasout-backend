package br.com.gasoutapp.dto;

import br.com.gasoutapp.domain.enums.RoomNameEnum;
import lombok.Data;

@Data
public class SensorDetailsDTO {
	private RoomNameEnum roomName;
	private boolean alarmOn;
	private boolean notificationOn;
	private boolean sprinklersOn;
	private Long sensorValue;
}