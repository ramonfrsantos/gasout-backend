package br.com.gasoutapp.dto;

import lombok.Data;

@Data
public class RoomRequiredDTO {
	private String name;
	private String userEmail;
	
	public RoomRequiredDTO() {}
}