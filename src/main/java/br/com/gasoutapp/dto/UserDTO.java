package br.com.gasoutapp.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.gasoutapp.domain.User;
import lombok.Data;

@Data
public class UserDTO {
	@Size(min = 2, message = "O nome deve conter no minimo 2 caracteres.")
	private String name;
	@Email(regexp = ".+[@].+[\\.].+")
	private String email;
	@JsonIgnore
	private String password;
	private String verificationCode;

	public UserDTO() {
	}

	public UserDTO(User user) {
		this.name = user.getName();
		this.email = user.getEmail();
		this.password = user.getPassword();
	}

	public UserDTO(String name, String email, String password) {
		this.name = name;
		this.email = email;
		this.password = password;
	}
}