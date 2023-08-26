package br.com.gasoutapp.domain.enums;

public enum RoomNameEnum {
	QUARTO(1, "QUARTO"), SALA_DE_ESTAR(2, "SALA DE ESTAR"), SALA_DE_JANTAR(3, "SALA DE JANTAR"),
	AREA_DE_LAZER(4, "AREA DE LAZER"), AREA_DA_CHURRASQUEIRA(5, "AREA DA CHURRASQUEIRA"), BANHEIRO(6, "BANHEIRO"),
	COZINHA(7, "COZINHA");

	private Integer nameId;
	private String nameDescription;

	RoomNameEnum(int nameId, String nameDescription) {
		this.nameId = nameId;
		this.nameDescription = nameDescription;
	}

	public Integer getNameId() {
		return nameId;
	}

	public void setNameId(Integer nameId) {
		this.nameId = nameId;
	}

	public String getNameDescription() {
		return nameDescription;
	}

	public void setNameDescription(String nameDescription) {
		this.nameDescription = nameDescription;
	}
}