package br.com.gasoutapp.security;

import java.util.Date;

import lombok.Data;

@Data
public class LoginResultDTO {

	private String userId;
	private String login;
	private String token;
	private String tokenType;
	private String refreshToken;
	private String userName;
	private Date tokenExpiresIn;
}
