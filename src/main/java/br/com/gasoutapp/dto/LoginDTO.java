package br.com.gasoutapp.dto;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class LoginDTO {
	@NotEmpty
	private String login;
	private String password;
	private String tokenFirebase;
}
