package br.com.gasoutapp.security;

import lombok.Data;

@Data
public class LoginResultDTO {

	private String userId;
	private String login;
	private String token;
	private String refreshToken;
	private String userName;
	private Long expiresIn;
}
