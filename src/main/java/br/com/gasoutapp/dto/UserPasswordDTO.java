package br.com.gasoutapp.dto;

import br.com.gasoutapp.domain.User;
import lombok.Data;

@Data
public class UserPasswordDTO {
	private String id;
	private String password;

	public UserPasswordDTO() {
	}

	public UserPasswordDTO(User user) {
		this.id = user.getId();
		this.password = user.getPassword();
	}
}