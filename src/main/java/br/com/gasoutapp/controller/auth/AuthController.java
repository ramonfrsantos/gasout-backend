package br.com.gasoutapp.controller.auth;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gasoutapp.config.security.LoginResultDTO;
import br.com.gasoutapp.config.security.TokenService;
import br.com.gasoutapp.domain.user.User;
import br.com.gasoutapp.dto.user.LoginDTO;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("auth")
@Tag(name = "Autenticação", description = "Serviços de validação e autenticação do usuário.")
public class AuthController {
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@PostMapping("/login")
	public LoginResultDTO login(@Valid @RequestBody LoginDTO dto) throws Exception {
		var usernamePassword = new UsernamePasswordAuthenticationToken(dto.getLogin(), dto.getPassword());
		var auth = this.authenticationManager.authenticate(usernamePassword);
		User user = (User) auth.getPrincipal();
		var token = tokenService.generateToken((User) auth.getPrincipal());
				
		return new LoginResultDTO(user.getLogin(), token, user.getName());
	}

//	@GetMapping("/validate-admin")
//	public String checkIfAdminExists() {
//		return service.checkIfAdminExists();
//	}

//	@GetMapping("/find-by-token")
//	public UserJWT getUserByToken(@RequestParam(required = true) String accessToken) {
//		return service.getUserByToken(accessToken);
//	}

//	@PostMapping("/refresh-token")
//	public LoginResultDTO login(@RequestParam String refreshToken) {
//		return service.refreshToken(refreshToken);
//	}

}