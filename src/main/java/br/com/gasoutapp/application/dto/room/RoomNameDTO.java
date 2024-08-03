package br.com.gasoutapp.application.dto.room;

import br.com.gasoutapp.infrastructure.db.entity.enums.RoomNameEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoomNameDTO {
	private Integer nameId;
	private String nameDescription;

	public RoomNameDTO(RoomNameEnum name) {
		this.nameId = name.getNameId();
		this.nameDescription = name.getNameDescription();
	}

	public RoomNameDTO(Integer nameId, String nameDescription) {
		this.nameId = nameId;
		this.nameDescription = nameDescription;
	}
}