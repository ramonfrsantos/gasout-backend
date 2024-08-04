package br.com.gasoutapp.domain.service.auth;

import br.com.gasoutapp.application.dto.LoginResultDTO;

public interface AuthService {

	String checkIfAdminExists();

	LoginResultDTO login(String login, String password);
}