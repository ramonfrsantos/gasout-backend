package br.com.gasoutapp.application.dto.user;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class LoginDTO {
	@NotEmpty
	private String login;
	private String password;
	private String tokenFirebase;
}
