package br.com.gasoutapp.application.dto.room;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoomRequiredDTO {
	private String name;
	private String userEmail;
}