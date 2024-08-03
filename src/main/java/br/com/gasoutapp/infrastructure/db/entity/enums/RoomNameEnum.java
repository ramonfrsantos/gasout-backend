package br.com.gasoutapp.infrastructure.db.entity.enums;

import lombok.Getter;

@Getter
public enum RoomNameEnum {
	QUARTO(1, "QUARTO"),
	SALA_DE_ESTAR(2, "SALA"),
	SALA_DE_JANTAR(3, "SALA DE JANTAR"),
	AREA_DE_LAZER(4, "AREA DE LAZER"),
	AREA_DA_CHURRASQUEIRA(5, "AREA CHURRASQUEIRA"),
	BANHEIRO(6, "BANHEIRO"),
	COZINHA(7, "COZINHA");

	private final Integer nameId;
	private final String nameDescription;

	RoomNameEnum(int nameId, String nameDescription) {
		this.nameId = nameId;
		this.nameDescription = nameDescription;
	}

}