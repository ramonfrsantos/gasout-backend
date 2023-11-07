package br.com.gasoutapp.application.dto.room;

import java.util.Date;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import br.com.gasoutapp.domain.entity.enums.SensorTypeEnum;
import br.com.gasoutapp.domain.entity.room.Sensor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SensorDTO {

	@Email
	private String userEmail;
	
	@NotNull
	private Integer roomNameId;
	
	@NotNull
	private Long sensorValue;
	
	@NotNull
	private SensorTypeEnum sensorType;
	
	@NotNull
	private Date timestamp;

	public SensorDTO(Sensor sensor) {
		this.userEmail = sensor.getRoom().getUserEmail();
		this.roomNameId = sensor.getRoom().getName().getNameId();
		this.sensorValue = sensor.getSensorValue();
		this.sensorType = sensor.getSensorType();
		this.timestamp = sensor.getTimestamp();
	}
	
}