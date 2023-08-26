package br.com.gasoutapp.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import br.com.gasoutapp.domain.enums.RoomNameEnum;
import lombok.Data;

@Data
public class SensorDetailsDTO {
	
	@Email
	private String userEmail;
	@NotNull
	private RoomNameEnum roomName;
	@NotNull
	private Long sensorValue;
}