package br.com.gasoutapp.application.dto.room;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import br.com.gasoutapp.infrastructure.db.entity.room.Sensor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SensorMinDetailsDTO {
	private Long sensorValue;
	private ZonedDateTime timestamp;

	public SensorMinDetailsDTO(Sensor sensor) {
		this.sensorValue = sensor.getSensorValue();	
		this.timestamp = sensor.getTimestamp().toInstant().atZone(ZoneId.of("America/Sao_Paulo"));
	}
}