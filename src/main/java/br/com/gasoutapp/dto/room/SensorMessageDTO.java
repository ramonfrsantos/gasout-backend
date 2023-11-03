package br.com.gasoutapp.dto.room;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class SensorMessageDTO {

	@Email
	private String userEmail;
	
	@NotNull
	private Integer roomNameId;
	
	@NotNull
	private Long gasSensorValue;

	private Long umiditySensorValue;
}