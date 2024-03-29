package br.com.gasoutapp.application.dto.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.gasoutapp.infrastructure.db.entity.user.User;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
	private String id;
	@Size(min = 2, message = "O nome deve conter no minimo 2 caracteres.")
	private String name;
	@Email(regexp = ".+[@].+[\\.].+")
	private String email;
	private String password;
	private String verificationCode;

	public UserDTO() {
	}

	public UserDTO(String id, String email) {
		this.id = id;
		this.email = email;
	}

	public UserDTO(String name, String email, String password) {
		this.name = name;
		this.email = email;
		this.password = password;
	}

	public UserDTO(User user) {
		this.id = user.getId();
		this.name = user.getName();
		this.email = user.getEmail();
	}

	public UserDTO(String email) {
		this.email = email;
	}
}