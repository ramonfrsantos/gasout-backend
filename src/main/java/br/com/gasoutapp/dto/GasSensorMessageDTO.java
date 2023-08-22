package br.com.gasoutapp.dto;

import br.com.gasoutapp.domain.enums.RoomNameEnum;

public class GasSensorMessageDTO {

	private Long sensorValue;
	private RoomNameEnum roomName;
	private String email;

	public Long getSensorValue() {
		return sensorValue;
	}

	public void setSensorValue(Long sensorValue) {
		this.sensorValue = sensorValue;
	}

	public RoomNameEnum getRoomName() {
		return roomName;
	}

	public void setRoomName(RoomNameEnum roomName) {
		this.roomName = roomName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
