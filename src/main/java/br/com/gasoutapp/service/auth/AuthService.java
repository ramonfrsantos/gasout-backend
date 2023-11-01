package br.com.gasoutapp.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.service.user.UserService;

@Service
public class AuthService implements UserDetailsService {

	@Autowired
	private UserService userService;

//	public String checkIfAdminExists() {
//		List<UserTypeEnum> roles = new ArrayList<UserTypeEnum>();
//		roles.add(UserTypeEnum.ADMIN);
//
//		List<User> admins = userService.findAllByRoles(UserTypeEnum.ADMIN);
//		if (admins == null || admins.size() == 0) {
//			User user = userService.create(new UserDTO(adminName, adminEmail, adminPassword));
//
//			String token = "";
//
//			try {
//				token = this.login(user.getLogin(), CriptexCustom.decrypt(user.getPassword()), null).getToken();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			return token;
//		} else {
//			return "Usuário [ADMIN] já existe no sistema.";
//		}
//	}

//	@ExceptionHandler({ Exception.class })
//	public LoginResultDTO login(String login, String password, String tokenFirebase) throws Exception {
//		if (password.length() < 6) {
//			throw new WrongPasswordException();
//		}
//		
//		User user = userService.findByLoginAndPassword(login, password);
//		if (user == null) {
//			User userLogin = userService.findByLogin(login);
//
//			if (userLogin == null) {
//				throw new NotFoundException("Dados de login incorretos.");
//			} else if (!userLogin.getPassword().equals(password)) {
//				throw new WrongPasswordException();
//			} else {
//				throw new NotFoundException("Usuario nao encontrado.");
//			}
//		} else {
//			return userService.getDtoByUser(user, tokenFirebase);
//		}
//	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userService.findByLogin(username);
	}
}