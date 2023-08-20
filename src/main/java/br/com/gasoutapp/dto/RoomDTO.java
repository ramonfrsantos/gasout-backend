package br.com.gasoutapp.dto;

import br.com.gasoutapp.domain.Room;
import lombok.Data;

@Data
public class RoomDTO {
	private String id;
	private String name;
	private String userEmail;
	private Long sensorValue;
	private UserDTO user;

	public RoomDTO() {
	}

	public RoomDTO(Room entity) {
		super();
		this.id = entity.getId();
		this.name = entity.getName();
		this.userEmail = entity.getUserEmail();
		this.sensorValue = entity.getSensorValue();

		if (entity.getUser() != null) {
			this.user = new UserDTO(entity.getUser().getId());
		}
	}
}