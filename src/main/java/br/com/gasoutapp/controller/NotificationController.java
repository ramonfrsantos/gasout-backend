package br.com.gasoutapp.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gasoutapp.domain.Notification;
import br.com.gasoutapp.dto.BaseResponseDTO;
import br.com.gasoutapp.dto.NotificationDTO;
import br.com.gasoutapp.exception.NotFoundException;
import br.com.gasoutapp.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("notifications")
public class NotificationController extends BaseRestController {
	@Autowired
	private NotificationService service;

	@GetMapping
	@Operation(summary = "Buscar todas as notificações", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO getAllNotifications() {
		return buildResponse(service.getAllNotifications());
	}

	@GetMapping("/recent/{email}")
	@Operation(summary = "Buscar notificações recentes por email", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO getAllRecentNotifications(@PathVariable String email) {
		return buildResponse(service.getAllRecentNotifications(email));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar notificação por id", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO findNotificationById(@PathVariable String id) {

		Optional<Notification> optNotification = service.findNotificationById(id);

		if (!optNotification.isPresent()) {
			throw new NotFoundException("Notificação não encontrada.");
		}

		EntityModel<NotificationDTO> model = EntityModel.of(new NotificationDTO(optNotification.get()));

		WebMvcLinkBuilder linkToUsers = linkTo(methodOn(this.getClass()).getAllNotifications());
		model.add(linkToUsers.withRel("all-notifications"));

		return buildResponse(model);
	}

	@PostMapping
	@Operation(summary = "Criar uma notificação", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO createNotification(@RequestBody NotificationDTO dto) {
		return buildResponse(service.createNotification(dto));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Excluir notificação por id", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO deleteNotification(@PathVariable String id) {
		return buildResponse(service.deleteNotification(id));
	}
}