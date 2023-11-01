package br.com.gasoutapp.controller.user;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gasoutapp.controller.BaseRestController;
import br.com.gasoutapp.domain.user.User;
import br.com.gasoutapp.dto.BaseResponseDTO;
import br.com.gasoutapp.dto.user.LoginDTO;
import br.com.gasoutapp.dto.user.UserDTO;
import br.com.gasoutapp.exception.NotFoundException;
import br.com.gasoutapp.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("users")
@Tag(name = "Usuário", description = "Serviços relacionados ao usuário.")
public class UserController extends BaseRestController {
	
	@Autowired
	private UserService service;

	@GetMapping("/revisions/{id}")
	@Operation(summary = "Buscar revisões do <i>envers</i>", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO getRevisions(@PathVariable String id) {
		return buildResponse(service.getRevisions(id));
	}

	@GetMapping
	@Operation(summary = "Buscar todos os usuários", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO findAll() {
		return buildResponse(service.findAll());
	}

	@GetMapping("/verification-code/{email}")
	@Operation(summary = "Buscar código de verificação por email", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO getVerificationCode(@PathVariable String email) throws Exception {
		return buildResponse(service.getVerificationCode(email));
	}

	@GetMapping("/check-codes-equal/{newCode}/{email}")
	@Operation(summary = "Verificar se o código de verificação é válido", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO checkIfCodesAreEqual(@PathVariable String email, @PathVariable String newCode)
			throws Exception {
		return buildResponse(service.checkIfCodesAreEqual(email, newCode));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar usuário por id", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO findUserById(@PathVariable String id) {
		Optional<User> optUser = service.findUserById(id);

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
		String encryptedPassword = new BCryptPasswordEncoder().encode(dto.getPassword());
		dto.setPassword(encryptedPassword);
		
		return buildResponse(service.register(dto));
	}

	@PutMapping("/refresh")
	@Operation(summary = "Atualizar a senha", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO sendVerificationMail(@RequestBody LoginDTO dto) throws Exception {
		return buildResponse(service.refreshPassword(dto));
	}

	@PutMapping("/send-verification-email/{email}")
	@Operation(summary = "Enviar email com código de verificação", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO sendVerificationMail(@PathVariable String email) throws Exception {
		return buildResponse(service.sendVerificationMail(email));
	}

	@DeleteMapping("/{email}")
	@Operation(summary = "Excluir usuário por email", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO delete(@PathVariable String email) throws Exception {
		return buildResponse(service.delete(email));
	}

}