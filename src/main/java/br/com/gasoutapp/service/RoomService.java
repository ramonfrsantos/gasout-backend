package br.com.gasoutapp.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

	public List<Room> getAllRooms() {
		return repository.findAll();
	}

	public List<Room> getAllUserRooms(String login) {
		return repository.findAllByUser(userService.findByLogin(login));
	}

	public ResponseEntity<Object> createRoom(RoomDTO dto) {
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
		newRoom = repository.save(newRoom);

		newUserRooms.add(newRoom);

		userService.setUserRooms(newUserRooms, newUser);

		URI locationRoom = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(newRoom.getId()).toUri();

		return ResponseEntity.created(locationRoom).build();
	}

	public Room sendRoomSensorValue(SensorDetailsDTO dto, String login) {
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

		return newRoom;
	}

	public void deleteRoom(String id) {
		Room room = repository.getById(id);
		if (room == null) {
			throw new NotFoundException("Cômodo com o id informado não está cadastrado.");
		} else {
			room.setDeleted(true);
			repository.save(room);
		}
	}

	public Optional<Room> findRoomById(String id) {
		return repository.findById(id);
	}
}