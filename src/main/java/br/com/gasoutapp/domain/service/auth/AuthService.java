package br.com.gasoutapp.domain.service.auth;

import br.com.gasoutapp.infrastructure.config.security.LoginResultDTO;
import br.com.gasoutapp.infrastructure.config.security.UserJWT;

public interface AuthService {

	public String checkIfAdminExists();

	public LoginResultDTO login(String login, String password, String tokenFirebase);

	public UserJWT getUserByToken(String token);

	public LoginResultDTO refreshToken(String refreshToken);
}