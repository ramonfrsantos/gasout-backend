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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gasoutapp.domain.Room;
import br.com.gasoutapp.dto.RoomDTO;
import br.com.gasoutapp.dto.SensorDetailsDTO;
import br.com.gasoutapp.exception.NotFoundException;
import br.com.gasoutapp.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("rooms")
public class RoomController {

	@Autowired
	private RoomService roomService;

	@GetMapping
	@Operation(summary = "Buscar todos os quartos", security = @SecurityRequirement(name = "gasoutapp"))
	public List<Room> getAllRooms() {
		return roomService.getAllRooms();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar quarto por id", security = @SecurityRequirement(name = "gasoutapp"))
	public EntityModel<Optional<Room>> findRoomById(@PathVariable String id) {

		Optional<Room> room = roomService.findRoomById(id);

		if (room == null) {
			throw new NotFoundException("Cômodo com o id informado não está cadastrado.");
		}

		EntityModel<Optional<Room>> model = EntityModel.of(room);

		WebMvcLinkBuilder linkToUsers = linkTo(methodOn(this.getClass()).getAllRooms());
		model.add(linkToUsers.withRel("all-rooms"));

		return model;
	}

	@PostMapping
	@Operation(summary = "Cadastrar quarto", security = @SecurityRequirement(name = "gasoutapp"))
	public ResponseEntity<Object> createRoom(@RequestBody RoomDTO dto) {
		return roomService.createRoom(dto);
	}

	@GetMapping("/find-all/{email}")
	@Operation(summary = "Buscar quartos por email", security = @SecurityRequirement(name = "gasoutapp"))
	public List<Room> getAllUserRooms(@PathVariable String email) {
		return roomService.getAllUserRooms(email);
	}

	@PutMapping("/sensor-measurement-details/{email}")
	@Operation(summary = "Atualizar medidas relativas ao sensor", security = @SecurityRequirement(name = "gasoutapp"))
	public Room sendRoomSensorValue(@RequestBody SensorDetailsDTO dto, @PathVariable String email) {
		return roomService.sendRoomSensorValue(dto, email);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Excluir quarto por id", security = @SecurityRequirement(name = "gasoutapp"))
	public void deleteRoom(@PathVariable String id) {
		roomService.deleteRoom(id);
	}
}