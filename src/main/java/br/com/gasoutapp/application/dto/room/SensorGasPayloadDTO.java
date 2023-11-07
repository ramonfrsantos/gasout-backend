package br.com.gasoutapp.application.dto.room;

import java.util.List;

public class SensorGasPayloadDTO {

	private List<SensorDTO> sensors;

	public List<SensorDTO> getSensors() {
		return sensors;
	}

	public void setSensors(List<SensorDTO> sensors) {
		this.sensors = sensors;
	}
	
}
