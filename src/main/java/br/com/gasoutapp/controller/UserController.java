package br.com.gasoutapp.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.gasoutapp.domain.User;
import br.com.gasoutapp.dto.LoginDTO;
import br.com.gasoutapp.dto.UserDTO;
import br.com.gasoutapp.exception.UserNotFoundException;
import br.com.gasoutapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("users")
public class UserController {
	@Autowired
	private UserService userService;

	@RequestMapping(method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@Operation(summary = "Buscar todos os usuários", security = @SecurityRequirement(name = "gasoutapp"))
	public List<User> findAll() {
		return userService.findAll();
	}

	@RequestMapping(method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@Operation(summary = "Registrar usuário no sistema", security = @SecurityRequirement(name = "gasoutapp"))
	public ResponseEntity<Object> register(@Valid @RequestBody UserDTO dto) throws Exception {
		return userService.register(dto);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@Operation(summary = "Buscar usuário por id", security = @SecurityRequirement(name = "gasoutapp"))
	public EntityModel<Optional<User>> findUserById(@PathVariable String id) {
		System.out.println();

		Optional<User> user = userService.findUserById(id);

		if (user == null) {
			throw new UserNotFoundException();
		}

		EntityModel<Optional<User>> model = EntityModel.of(user);

		WebMvcLinkBuilder linkToUsers = linkTo(methodOn(this.getClass()).findAll());
		model.add(linkToUsers.withRel("all-users"));

		return model;
	}

	@RequestMapping(path = { "/{email}" }, method = RequestMethod.DELETE, produces = "application/json;charset=UTF-8")
	@Operation(summary = "Excluir usuário por email", security = @SecurityRequirement(name = "gasoutapp"))
	public void delete(@PathVariable String email) throws Exception {
		userService.delete(email);
	}

	@RequestMapping(path = { "/refresh" }, method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
	@Operation(summary = "Atualizar a senha", security = @SecurityRequirement(name = "gasoutapp"))
	public User sendVerificationMail(@RequestBody LoginDTO dto) throws Exception {
		return userService.refreshPassword(dto);
	}

	@RequestMapping(path = {
			"/send-verification-email/{email}" }, method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
	@Operation(summary = "Enviar email com código de verificação", security = @SecurityRequirement(name = "gasoutapp"))
	public String sendVerificationMail(@PathVariable String email) throws Exception {
		return userService.sendVerificationMail(email);
	}

	@RequestMapping(path = {
			"/verification-code/{email}" }, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@Operation(summary = "Buscar código de verificação por email", security = @SecurityRequirement(name = "gasoutapp"))
	public String getVerificationCode(@PathVariable String email) throws Exception {
		return userService.getVerificationCode(email);
	}

	@RequestMapping(path = {
			"/check-codes-equal/{newCode}/{email}" }, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@Operation(summary = "Verificar se o código de verificação é válido", security = @SecurityRequirement(name = "gasoutapp"))
	public boolean checkIfCodesAreEqual(@PathVariable String email, @PathVariable String newCode) throws Exception {
		return userService.checkIfCodesAreEqual(email, newCode);
	}
}