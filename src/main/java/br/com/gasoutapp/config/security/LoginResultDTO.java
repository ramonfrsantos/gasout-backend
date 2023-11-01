package br.com.gasoutapp.config.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResultDTO {

	private String login;
	private String token;
	private String userName;
}
