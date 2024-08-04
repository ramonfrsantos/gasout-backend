package br.com.gasoutapp.domain.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.application.dto.user.UserDTO;
import br.com.gasoutapp.domain.exception.NotFoundException;
import br.com.gasoutapp.domain.exception.WrongPasswordException;
import br.com.gasoutapp.domain.service.user.UserService;
import br.com.gasoutapp.infrastructure.config.security.EncryptorCustom;
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

			token = this.login(user.getLogin(), EncryptorCustom.decrypt(user.getPassword())).getToken();

			return token;
		} else {
			return "Usuário [ADMIN] já existe no sistema.";
		}
	}

	@Override
	public LoginResultDTO login(String login, String password) {
		var user = userService.findByLogin(login);

		if (user == null) {
			throw new NotFoundException("Dados de login incorretos.");
		}
		var pass = EncryptorCustom.decrypt(user.getPassword());

		if(!password.equals(pass) || password.length() < 6){
			throw new WrongPasswordException("Senha incorreta.");
		}

		return userService.getDtoByUser(user);
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