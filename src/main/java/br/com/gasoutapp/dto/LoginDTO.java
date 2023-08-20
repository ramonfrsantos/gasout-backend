package br.com.gasoutapp.dto;

import lombok.Data;

@Data
public class LoginDTO {
	private String login;
	private String password;
	private String tokenFirebase;
}
