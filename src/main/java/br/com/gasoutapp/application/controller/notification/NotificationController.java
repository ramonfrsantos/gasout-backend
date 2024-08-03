package br.com.gasoutapp.application.controller.notification;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.IOException;
import java.net.URISyntaxException;

import br.com.gasoutapp.application.dto.notification.NotificationGeneratorDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.gasoutapp.application.controller.BaseRestController;
import br.com.gasoutapp.application.dto.BaseResponseDTO;
import br.com.gasoutapp.application.dto.notification.NotificationDTO;
import br.com.gasoutapp.application.dto.room.SensorGasPayloadDTO;
import br.com.gasoutapp.domain.exception.NotFoundException;
import br.com.gasoutapp.domain.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("notifications")
@Tag(name = "Notificações", description = "Serviços relacionados a parte de notificações do app.")
public class NotificationController extends BaseRestController {
	
	@Autowired
	private NotificationService service;
	
	@GetMapping("/revisions/{id}")
	@Operation(summary = "Buscar revisões do <i>envers</i>", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO getRevisions(@PathVariable String id) {
		return buildResponse(service.getRevisions(id));
	}

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
		var optNotification = service.findNotificationById(id);

		if (optNotification.isEmpty()) {
			throw new NotFoundException("Notificação não encontrada.");
		}

		var linkToUsers = linkTo(methodOn(this.getClass()).getAllNotifications());

		var model = EntityModel.of(new NotificationDTO(optNotification.get()));
		model.add(linkToUsers.withRel("all-notifications"));

		return buildResponse(model);
	}

	@PostMapping
	@Operation(summary = "Criar uma notificação", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO createNotification(@RequestBody NotificationGeneratorDTO generatorDTO) {
		var dto = new NotificationDTO(generatorDTO.getMessage(), generatorDTO.getTitle(), generatorDTO.getUserEmail());
		var newNotification = service.createNotification(dto);

		var locationNotification = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(newNotification.getId()).toUri();
		
		return buildResponse(ResponseEntity.created(locationNotification).body(newNotification));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Excluir notificação por id", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO deleteNotification(@PathVariable String id) {
		return buildResponse(service.deleteNotification(id));
	}

	@PostMapping("/push")
	@Operation(summary = "Enviar push notification", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO sendPush(@RequestBody SensorGasPayloadDTO payload) throws IOException, URISyntaxException {		
		return buildResponse(service.sendPush(payload));
	}
}