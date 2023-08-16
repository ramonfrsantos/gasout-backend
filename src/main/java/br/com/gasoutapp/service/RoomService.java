package br.com.gasoutapp.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.gasoutapp.domain.Room;
import br.com.gasoutapp.domain.User;
import br.com.gasoutapp.dto.RoomDTO;
import br.com.gasoutapp.dto.SensorDetailsDTO;
import br.com.gasoutapp.exception.AlreadyExistsException;
import br.com.gasoutapp.exception.NotFoundException;
import br.com.gasoutapp.repository.RoomRepository;

@Service
public class RoomService {

	@Autowired
	private RoomRepository repository;

	@Autowired
	private UserService userService;

	public List<RoomDTO> parseToDTO(List<Room> list) {
		return list.stream().map(v -> parseToDTO(v)).collect(Collectors.toList());
	}

	public Page<RoomDTO> parseToDTO(Page<Room> page) {
		return page.map(RoomDTO::new);
	}

	public RoomDTO parseToDTO(Room room) {
		return new RoomDTO(room);
	}

	public List<RoomDTO> getAllRooms() {
		return parseToDTO(repository.findAll());
	}

	public List<RoomDTO> getAllUserRooms(String login) {
		return parseToDTO(repository.findAllByUser(userService.findByLogin(login)));
	}

	public ResponseEntity<RoomDTO> createRoom(RoomDTO dto) {
		List<Room> newUserRooms;
		User newUser;

		User user = userService.findByLogin(dto.getUser().getEmail());
		if (user == null) {
			throw new NotFoundException("Usuario nao encontrado.");
		}
		newUser = user;

		List<Room> rooms = repository.findAllByUser(user);
		for (Room room : rooms) {
			if (room.getName().equals(dto.getName())) {
				throw new AlreadyExistsException("Esse cômodo já foi cadastrado.");
			}
		}
		newUserRooms = rooms;

		Room newRoom = new Room();

		newRoom.setUser(user);
		newRoom.setName(dto.getName());
		newRoom.setUserEmail(user.getEmail());
		newRoom.setSensorValue(0);
		newRoom = repository.save(newRoom);

		newUserRooms.add(newRoom);

		userService.setUserRooms(newUserRooms, newUser);

		URI locationRoom = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(newRoom.getId()).toUri();

		return ResponseEntity.created(locationRoom).body(parseToDTO(newRoom));
	}

	public RoomDTO sendRoomSensorValue(SensorDetailsDTO dto, String login) {
		Room newRoom = new Room();

		User user = userService.findByLogin(login);
		if (user == null) {
			throw new NotFoundException("Usuario nao encontrado.");
		}

		List<Room> rooms = repository.findAllByUser(user);
		for (Room room : rooms) {
			if (room.getName().equalsIgnoreCase(dto.getName())) {
				newRoom = room;
				newRoom.setSensorValue(dto.getSensorValue());
				newRoom.setAlarmOn(dto.isAlarmOn());
				newRoom.setNotificationOn(dto.isNotificationOn());
				newRoom.setSprinklersOn(dto.isSprinklersOn());
				repository.save(newRoom);
			}
		}

		return parseToDTO(newRoom);
	}

	public String deleteRoom(String id) {
		Room room = repository.getById(id);
		if (room == null) {
			throw new NotFoundException("Cômodo com o id informado não está cadastrado.");
		} else {
			room.setDeleted(true);
			repository.save(room);
		}

		return "Registro excluido com sucesso.";
	}

	public Optional<Room> findRoomById(String id) {
		return repository.findById(id);
	}
}