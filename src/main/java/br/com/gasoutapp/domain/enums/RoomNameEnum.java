package br.com.gasoutapp.domain.enums;

public enum RoomNameEnum {
	QUARTO(1, "QUARTO"), SALA_DE_ESTAR(2, "SALA DE ESTAR"), SALA_DE_JANTAR(3, "SALA DE JANTAR"),
	AREA_DE_LAZER(4, "AREA DE LAZER"), AREA_DA_CHURRASQUEIRA(5, "AREA DA CHURRASQUEIRA"), BANHEIRO(6, "BANHEIRO"),
	COZINHA(7, "COZINHA");

	private Integer id;
	private String descricao;

	RoomNameEnum(int id, String descricao) {
		this.id = id;
		this.descricao = descricao;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
}
