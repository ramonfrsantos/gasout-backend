package br.com.gasoutapp.dto.room;

import lombok.Data;

@Data
public class RoomRequiredDTO {
	private String name;
	private String userEmail;
	
	public RoomRequiredDTO() {}
}