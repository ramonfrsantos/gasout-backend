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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.gasoutapp.domain.Room;
import br.com.gasoutapp.domain.enums.RoomNameEnum;
import br.com.gasoutapp.dto.BaseResponseDTO;
import br.com.gasoutapp.dto.RoomDTO;
import br.com.gasoutapp.dto.SensorDetailsDTO;
import br.com.gasoutapp.exception.NotFoundException;
import br.com.gasoutapp.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("rooms")
public class RoomController extends BaseRestController {

	@Autowired
	private RoomService service;

	@GetMapping
	@Operation(summary = "Buscar todos os quartos", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO getAllRooms() {
		return buildResponse(service.getAllRooms());
	}

	@GetMapping("/find/{id}")
	@Operation(summary = "Buscar quarto por id", security = @SecurityRequirement(name = "gasoutapp"))
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
	@Operation(summary = "Buscar quartos por email e por nome", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO getAllUserRooms(@PathVariable String email, @RequestParam(required = false) RoomNameEnum roomName) {
		return buildResponse(service.getAllUserRooms(email, roomName));
	}

	@PostMapping
	@Operation(summary = "Cadastrar quarto", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO createRoom(@RequestBody RoomDTO dto) {
		return buildResponse(service.createRoom(dto));
	}

	@PutMapping("/sensor-measurement-details/{email}")
	@Operation(summary = "Atualizar medidas relativas ao sensor", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO sendRoomSensorValue(@RequestBody SensorDetailsDTO dto, @PathVariable String email) {
		return buildResponse(service.sendRoomSensorValue(dto, email));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Excluir quarto por id", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO deleteRoom(@PathVariable String id) {
		return buildResponse(service.deleteRoom(id));
	}
	
	@DeleteMapping("/delete-all")
	@Operation(summary = "Excluir quartos", security = @SecurityRequirement(name = "gasoutapp"))
	public void deleteAll() {
		service.deleteAll();
	}
}