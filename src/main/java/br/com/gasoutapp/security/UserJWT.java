package br.com.gasoutapp.security;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class UserJWT {

	@JsonIgnore
	private String id;
	private String userLogin;
	private Long tokenExpiresIn;
	private boolean isValid;

	public UserJWT(String id, String userLogin, Long expiresIn, boolean isValid) {
		this.id = id;
		this.userLogin = userLogin;
		this.tokenExpiresIn = expiresIn;
		this.isValid = isValid;
	}
}