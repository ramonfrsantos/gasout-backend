package br.com.gasoutapp.domain.service.auth;

import br.com.gasoutapp.application.dto.LoginResultDTO;
import br.com.gasoutapp.infrastructure.config.security.UserJWT;

public interface AuthService {

	String checkIfAdminExists();

	LoginResultDTO login(String login, String password);

	UserJWT getUserByToken(String token);

	LoginResultDTO refreshToken(String refreshToken);
}