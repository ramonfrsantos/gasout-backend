package br.com.gasoutapp.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gasoutapp.domain.User;
import br.com.gasoutapp.dto.BaseResponseDTO;
import br.com.gasoutapp.dto.LoginDTO;
import br.com.gasoutapp.dto.UserDTO;
import br.com.gasoutapp.exception.NotFoundException;
import br.com.gasoutapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("users")
public class UserController extends BaseRestController {
	@Autowired
	private UserService userService;

	@GetMapping
	@Operation(summary = "Buscar todos os usuários", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO findAll() {
		return buildResponse(userService.findAll());
	}

	@GetMapping("/verification-code/{email}")
	@Operation(summary = "Buscar código de verificação por email", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO getVerificationCode(@PathVariable String email) throws Exception {
		return buildResponse(userService.getVerificationCode(email));
	}

	@GetMapping("/check-codes-equal/{newCode}/{email}")
	@Operation(summary = "Verificar se o código de verificação é válido", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO checkIfCodesAreEqual(@PathVariable String email, @PathVariable String newCode)
			throws Exception {
		return buildResponse(userService.checkIfCodesAreEqual(email, newCode));
	}
	
	@GetMapping("/get-user-pass/{email}")
	@Operation(summary = "Buscar senha do usuário encriptada", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO getUserPassword(@PathVariable String email)
			throws Exception {
		return buildResponse(userService.getUserPassword(email));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar usuário por id", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO findUserById(@PathVariable String id) {
		Optional<User> optUser = userService.findUserById(id);

		if (!optUser.isPresent()) {
			throw new NotFoundException("Usuario nao encontrado.");
		}

		EntityModel<UserDTO> model = EntityModel.of(new UserDTO(optUser.get()));

		WebMvcLinkBuilder linkToUsers = linkTo(methodOn(this.getClass()).findAll());
		model.add(linkToUsers.withRel("all-users"));

		return buildResponse(model);
	}

	@PostMapping
	@Operation(summary = "Registrar usuário no sistema", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO register(@Valid @RequestBody UserDTO dto) throws Exception {
		return buildResponse(userService.register(dto));
	}

	@PutMapping("/refresh")
	@Operation(summary = "Atualizar a senha", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO sendVerificationMail(@RequestBody LoginDTO dto) throws Exception {
		return buildResponse(userService.refreshPassword(dto));
	}

	@PutMapping("/send-verification-email/{email}")
	@Operation(summary = "Enviar email com código de verificação", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO sendVerificationMail(@PathVariable String email) throws Exception {
		return buildResponse(userService.sendVerificationMail(email));
	}

	@DeleteMapping("/{email}")
	@Operation(summary = "Excluir usuário por email", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO delete(@PathVariable String email) throws Exception {
		return buildResponse(userService.delete(email));
	}

}