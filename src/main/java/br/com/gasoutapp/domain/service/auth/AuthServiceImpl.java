package br.com.gasoutapp.domain.service.auth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.application.dto.user.UserDTO;
import br.com.gasoutapp.domain.exception.NotFoundException;
import br.com.gasoutapp.domain.exception.WrongPasswordException;
import br.com.gasoutapp.domain.service.user.UserService;
import br.com.gasoutapp.infrastructure.config.security.CriptexCustom;
import br.com.gasoutapp.infrastructure.config.security.LoginResultDTO;
import br.com.gasoutapp.infrastructure.config.security.TokenService;
import br.com.gasoutapp.infrastructure.config.security.UserJWT;
import br.com.gasoutapp.infrastructure.db.entity.enums.UserTypeEnum;
import br.com.gasoutapp.infrastructure.db.entity.user.User;

@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private UserService userService;

	@Autowired
	private TokenService tokenService;

	@Value("${spring.mail.username}")
	private String companyEmail;

	@Value("${user.admin.email}")
	private String adminEmail;

	@Value("${user.admin.password}")
	private String adminPassword;

	@Value("${user.admin.name}")
	private String adminName;

	@Override
	public String checkIfAdminExists() {
		List<UserTypeEnum> roles = new ArrayList<>();
		roles.add(UserTypeEnum.ADMIN);

		List<User> admins = userService.findAllByRoles(UserTypeEnum.ADMIN);
		if (admins == null || admins.isEmpty()) {
			User user = userService.create(new UserDTO(adminName, adminEmail, adminPassword));

			String token = "";

			try {
				token = this.login(user.getLogin(), CriptexCustom.decrypt(user.getPassword()), null).getToken();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return token;
		} else {
			return "Usuário [ADMIN] já existe no sistema.";
		}
	}

	@Override
	public LoginResultDTO login(String login, String password, String tokenFirebase) {
		if (password.length() < 6) {
			throw new WrongPasswordException();
		}
		password = CriptexCustom.encrypt(password);
		User user = userService.findByLoginAndPassword(login, password);
		User userLogin = userService.findByLogin(login);
		if (user == null) {
			if (userLogin == null) {
				throw new NotFoundException("Dados de login incorretos.");
			} else if (!userLogin.getPassword().equals(password)) {
				throw new WrongPasswordException();
			} else {
				throw new NotFoundException("Usuario nao encontrado.");
			}
		} else {
			return userService.getDtoByUser(user, tokenFirebase);
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