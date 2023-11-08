package br.com.gasoutapp.application.dto.room;

import java.util.Date;

import br.com.gasoutapp.infrastructure.db.entity.room.Sensor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SensorMinDetailsDTO {
	private Long sensorValue;
	private Date timestamp;

	public SensorMinDetailsDTO(Sensor sensor) {
		this.sensorValue = sensor.getSensorValue();
		this.timestamp = sensor.getTimestamp();
	}
}