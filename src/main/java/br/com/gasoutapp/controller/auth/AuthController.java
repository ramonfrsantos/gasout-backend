package br.com.gasoutapp.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.gasoutapp.config.security.LoginResultDTO;
import br.com.gasoutapp.config.security.UserJWT;
import br.com.gasoutapp.dto.user.LoginDTO;
import br.com.gasoutapp.service.auth.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("auth")
@Tag(name = "Autenticação", description = "Serviços de validação e autenticação do usuário.")
public class AuthController {

	@Autowired
	private AuthService service;

	@GetMapping("/validate-admin")
	public String checkIfAdminExists() {
		return service.checkIfAdminExists();
	}

	@GetMapping("/find-by-token")
	public UserJWT getUserByToken(@RequestParam(required = true) String accessToken) {
		return service.getUserByToken(accessToken);
	}

	@PostMapping("/login")
	public LoginResultDTO login(@RequestBody LoginDTO dto) throws Exception {
		return service.login(dto.getLogin(), dto.getPassword(), dto.getTokenFirebase());
	}

	@PostMapping("/refresh-token")
	public LoginResultDTO login(@RequestParam String refreshToken) {
		return service.refreshToken(refreshToken);
	}

}