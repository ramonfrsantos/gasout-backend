package br.com.gasoutapp.controller.room;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.gasoutapp.controller.BaseRestController;
import br.com.gasoutapp.domain.enums.RoomNameEnum;
import br.com.gasoutapp.domain.room.Room;
import br.com.gasoutapp.dto.BaseResponseDTO;
import br.com.gasoutapp.dto.room.RoomDTO;
import br.com.gasoutapp.dto.room.RoomSwitchesDTO;
import br.com.gasoutapp.dto.room.SensorMessageDTO;
import br.com.gasoutapp.exception.NotFoundException;
import br.com.gasoutapp.service.room.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("rooms")
@Tag(name = "Cômodos", description = "Serviços relacionados a parte de cômodos.")
public class RoomController extends BaseRestController {

	@Autowired
	private RoomService service;

	@GetMapping("/revisions/{id}")
	@Operation(summary = "Buscar revisões do <i>envers</i>", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO getRevisions(@PathVariable String id) {
		return buildResponse(service.getRevisions(id));
	}

	@GetMapping
	@Operation(summary = "Buscar cômodos possíveis para cadastro", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO getAllRooms() {
		return buildResponse(service.getAllRooms());
	}

	@GetMapping("/find/{id}")
	@Operation(summary = "Buscar cômodo por id", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO findRoomById(@PathVariable String id) {

		Optional<Room> optRoom = service.findRoomById(id);

		if (!optRoom.isPresent()) {
			throw new NotFoundException("Cômodo com o id informado não está cadastrado.");
		}

		EntityModel<RoomDTO> model = EntityModel.of(new RoomDTO(optRoom.get()));

		WebMvcLinkBuilder linkToUsers = linkTo(methodOn(this.getClass()).getAllRooms());
		model.add(linkToUsers.withRel("all-rooms"));

		return buildResponse(model);
	}

	@GetMapping("/{email}")
	@Operation(summary = "Buscar cômodos por email e por id", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO getAllUserRooms(@PathVariable String email,
			@RequestParam(required = false) Integer roomNameId) {
		return buildResponse(service.getAllUserRooms(email, roomNameId));
	}

	@PostMapping
	@Operation(summary = "Cadastrar cômodo", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO createRoom(@RequestParam RoomNameEnum roomName, @RequestParam String userEmail) {
		return buildResponse(service.createRoom(roomName, userEmail));
	}

	@PutMapping
	@Operation(summary = "Atualizar switches do cômodo", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO sendRoomSensorValue(@Valid @RequestBody RoomSwitchesDTO dto) {
		return buildResponse(service.updateSwitches(dto));
	}

	@PutMapping("/sensor-measurement-details")
	@Operation(summary = "Atualizar medidas relativas ao sensor", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO sendRoomSensorValue(@RequestBody SensorMessageDTO dto) {
		return buildResponse(service.sendRoomSensorValue(dto));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Excluir cômodo por id", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO deleteRoom(@PathVariable String id) {
		return buildResponse(service.deleteRoom(id));
	}

	@DeleteMapping("/delete-all/{userEmail}")
	@Operation(summary = "Excluir todos os cômodos do usuário", security = @SecurityRequirement(name = "gasoutapp"))
	public void deleteAll(@PathVariable String userEmail) {
		service.deleteAllByUser(userEmail);
	}
}