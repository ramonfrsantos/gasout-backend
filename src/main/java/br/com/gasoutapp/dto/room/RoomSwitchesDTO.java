package br.com.gasoutapp.dto.room;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoomSwitchesDTO {
	@NotNull
	private Integer nameId;
	@Email
	private String userEmail;
	private Boolean notificationOn;
	private Boolean alarmOn;
	private Boolean sprinklersOn;
}