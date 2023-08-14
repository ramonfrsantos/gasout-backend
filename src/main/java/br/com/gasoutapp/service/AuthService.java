package br.com.gasoutapp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import br.com.gasoutapp.domain.User;
import br.com.gasoutapp.domain.enums.UserTypeEnum;
import br.com.gasoutapp.dto.UserDTO;
import br.com.gasoutapp.exception.NotFoundException;
import br.com.gasoutapp.exception.WrongPasswordException;
import br.com.gasoutapp.security.CriptexCustom;
import br.com.gasoutapp.security.LoginResultDTO;
import br.com.gasoutapp.security.TokenService;
import br.com.gasoutapp.security.UserJWT;

@Service
public class AuthService {

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

	public String checkIfAdminExists() {
		List<UserTypeEnum> roles = new ArrayList<UserTypeEnum>();
		roles.add(UserTypeEnum.ADMIN);

		List<User> admins = userService.findAllByRoles(UserTypeEnum.ADMIN);
		if (admins == null || admins.size() == 0) {
			userService.create(new UserDTO(adminName, adminEmail, adminPassword));

			String token = "";

			try {
				token = this.login(adminEmail, adminPassword).getToken();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return token;
		} else {
			return "Usuário [ADMIN] já existe no sistema.";
		}
	}

	@ExceptionHandler({ Exception.class })
	public LoginResultDTO login(String login, String password) throws Exception {
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
			return userService.getDtoByUser(user);
		}
	}

	public UserJWT getUserByToken(String token) {
		return tokenService.getUserJWTFromToken(token);
	}

	public LoginResultDTO refreshToken(String refreshToken) {
		return tokenService.refreshToken(refreshToken);
	}
}