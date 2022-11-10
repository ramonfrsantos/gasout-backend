package br.com.gasoutapp.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.com.gasoutapp.domain.Room;
import br.com.gasoutapp.dto.RoomRequiredDTO;
import br.com.gasoutapp.dto.SensorDetailsDTO;
import br.com.gasoutapp.exception.RoomNotFoundException;
import br.com.gasoutapp.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("rooms")
public class RoomController {
	@Autowired
	private RoomService roomService;

	@RequestMapping(method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@Operation(summary = "Buscar todos os quartos", security = @SecurityRequirement(name = "gasoutapp"))
	public List<Room> getAllRooms() {
		return roomService.getAllRooms();
	}

	@RequestMapping(value = "/{email}/{roomName}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@Operation(summary = "Buscar quarto por nome", security = @SecurityRequirement(name = "gasoutapp"))
	public EntityModel<Room> findRoomById(@PathVariable String email, @PathVariable String roomName) {

		Room room = roomService.findRoomByName(email, roomName);

		if (room == null) {
			throw new RoomNotFoundException();
		}

		EntityModel<Room> model = EntityModel.of(room);

		WebMvcLinkBuilder linkToUsers = linkTo(methodOn(this.getClass()).getAllRooms());
		model.add(linkToUsers.withRel("all-rooms"));

		return model;
	}

	@RequestMapping(method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@Operation(summary = "Cadastrar quarto", security = @SecurityRequirement(name = "gasoutapp"))
	public ResponseEntity<Object> createRoom(@RequestBody RoomRequiredDTO dto) {
		return roomService.createRoom(dto);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "application/json;charset=UTF-8")
	@Operation(summary = "Excluir quarto por id", security = @SecurityRequirement(name = "gasoutapp"))
	public void deleteRoom(@PathVariable String id) {
		roomService.deleteRoom(id);
	}

	@RequestMapping(value = "/find-all/{email}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
	@Operation(summary = "Buscar quartos por email", security = @SecurityRequirement(name = "gasoutapp"))
	public List<Room> getAllUserRooms(@PathVariable String email) {
		return roomService.getAllUserRooms(email);
	}

	@RequestMapping(value = "/sensor-measurement-details/{email}", method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
	@Operation(summary = "Atualizar medidas relativas ao sensor", security = @SecurityRequirement(name = "gasoutapp"))
	public Room sendRoomSensorValue(@RequestBody SensorDetailsDTO dto, @PathVariable String email) {
		return roomService.sendRoomSensorValue(dto, email);
	}
}