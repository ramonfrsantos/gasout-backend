package br.com.gasoutapp.domain.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.application.dto.user.UserDTO;
import br.com.gasoutapp.domain.exception.NotFoundException;
import br.com.gasoutapp.domain.exception.WrongPasswordException;
import br.com.gasoutapp.domain.service.user.UserService;
import br.com.gasoutapp.infrastructure.config.security.CriptexCustom;
import br.com.gasoutapp.application.dto.LoginResultDTO;
import br.com.gasoutapp.infrastructure.config.security.TokenService;
import br.com.gasoutapp.infrastructure.config.security.UserJWT;
import br.com.gasoutapp.infrastructure.db.entity.enums.UserTypeEnum;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

	@Autowired
	private UserService userService;

	@Autowired
	private TokenService tokenService;

	@Value("${user.admin.email}")
	private String adminEmail;

	@Value("${user.admin.password}")
	private String adminPassword;

	@Value("${user.admin.name}")
	private String adminName;

	@Override
	public String checkIfAdminExists() {
		var admins = userService.findAllByRoles(UserTypeEnum.ADMIN);

		if (admins == null || admins.isEmpty()) {
			var user = userService.create(new UserDTO(adminName, adminEmail, adminPassword));

			var token = "";

			try {
				token = this.login(user.getLogin(), CriptexCustom.decrypt(user.getPassword())).getToken();
			} catch (Exception e) {
				log.error("Error = {}", e.getMessage());
			}

			return token;
		} else {
			return "Usuário [ADMIN] já existe no sistema.";
		}
	}

	@Override
	public LoginResultDTO login(String login, String password) {
		if (password.length() < 6) {
			throw new WrongPasswordException("Senha incorreta.");
		}
		password = CriptexCustom.encrypt(password);
		var user = userService.findByLoginAndPassword(login, password);
		var userLogin = userService.findByLogin(login);

		if (user == null) {
			if (userLogin == null) {
				throw new NotFoundException("Dados de login incorretos.");
			} else if (!userLogin.getPassword().equals(password)) {
				throw new WrongPasswordException("Senha incorreta.");
			} else {
				throw new NotFoundException("Usuario nao encontrado.");
			}
		} else {
			return userService.getDtoByUser(user);
		}
	}

	@Override
	public UserJWT getUserByToken(String token) {
		return tokenService.getUserJWTFromToken(token);
	}

	@Override
	public LoginResultDTO refreshToken(String refreshToken) {
		return tokenService.refreshToken(refreshToken);
	}
}