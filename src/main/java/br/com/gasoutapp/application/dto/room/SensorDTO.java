package br.com.gasoutapp.application.dto.room;

import java.util.Date;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import br.com.gasoutapp.infrastructure.db.entity.enums.SensorTypeEnum;
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
	
}