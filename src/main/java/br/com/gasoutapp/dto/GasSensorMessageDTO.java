package br.com.gasoutapp.dto;

public class GasSensorMessageDTO {

	private Long sensorValue;
	private String roomName;
	private String email;

	public Long getSensorValue() {
		return sensorValue;
	}

	public void setSensorValue(Long sensorValue) {
		this.sensorValue = sensorValue;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
