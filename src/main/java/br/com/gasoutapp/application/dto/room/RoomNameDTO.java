package br.com.gasoutapp.application.dto.room;

import br.com.gasoutapp.infrastructure.db.entity.enums.RoomNameEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomNameDTO {
	private Integer nameId;
	private String nameDescription;

	public RoomNameDTO(RoomNameEnum name) {
		this.nameId = name.getNameId();
		this.nameDescription = name.getNameDescription();
	}
}