package br.com.gasoutapp.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gasoutapp.domain.Notification;
import br.com.gasoutapp.dto.NotificationDTO;
import br.com.gasoutapp.exception.NotFoundException;
import br.com.gasoutapp.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("notifications")
public class NotificationController {
	@Autowired
	private NotificationService notificationService;

	@GetMapping
	@Operation(summary = "Buscar todas as notificações", security = @SecurityRequirement(name = "gasoutapp"))
	public List<Notification> getAllNotifications() {
		return notificationService.getAllNotifications();
	}

	@GetMapping("/recent/{email}")
	@Operation(summary = "Buscar notificações recentes por email", security = @SecurityRequirement(name = "gasoutapp"))
	public List<Notification> getAllRecentNotifications(@PathVariable String email) {
		return notificationService.getAllRecentNotifications(email);
	}

	@PostMapping
	@Operation(summary = "Criar uma notificação", security = @SecurityRequirement(name = "gasoutapp"))
	public ResponseEntity<Object> createNotification(@RequestBody NotificationDTO dto) {
		return notificationService.createNotification(dto);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar notificação por id", security = @SecurityRequirement(name = "gasoutapp"))
	public EntityModel<Optional<Notification>> findNotificationById(@PathVariable String id) {

		Optional<Notification> notification = notificationService.findNotificationById(id);

		if (notification == null) {
			throw new NotFoundException("Notificação não encontrada.");
		}

		EntityModel<Optional<Notification>> model = EntityModel.of(notification);

		WebMvcLinkBuilder linkToUsers = linkTo(methodOn(this.getClass()).getAllNotifications());
		model.add(linkToUsers.withRel("all-notifications"));

		return model;
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Excluir notificação por id", security = @SecurityRequirement(name = "gasoutapp"))
	public void deleteNotification(@PathVariable String id) {
		notificationService.deleteNotification(id);
	}
}