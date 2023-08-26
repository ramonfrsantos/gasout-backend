package br.com.gasoutapp.service;

import static br.com.gasoutapp.utils.JsonUtil.addKeysToJsonArray;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.gasoutapp.domain.Room;
import br.com.gasoutapp.domain.User;
import br.com.gasoutapp.domain.enums.RoomNameEnum;
import br.com.gasoutapp.dto.RevisionDetailsDTO;
import br.com.gasoutapp.dto.RoomDTO;
import br.com.gasoutapp.dto.RoomNameDTO;
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

	@Autowired
	private AuditReader auditReader;

	public List<RoomDTO> parseToDTO(List<Room> list) {
		return list.stream().map(v -> parseToDTO(v)).collect(Collectors.toList());
	}

	public Page<RoomDTO> parseToDTO(Page<Room> page) {
		return page.map(RoomDTO::new);
	}

	public RoomDTO parseToDTO(Room room) {
		return new RoomDTO(room);
	}

	public List<RoomNameDTO> getAllRooms() {
			return Arrays.asList(RoomNameEnum.values()).stream().map(room -> new RoomNameDTO(room.getId(), room.getDescricao())).toList();
	}

	public List<RoomDTO> getAllUserRooms(String login, RoomNameEnum roomName) {
		List<RoomDTO> rooms = new ArrayList<RoomDTO>();

		if (roomName == null) {
			rooms = parseToDTO(repository.findAllByUser(userService.findByLogin(login)));
		} else {
			RoomDTO roomDTO = getUserRoomByName(login, roomName);
			if (roomDTO != null) {
				rooms.add(roomDTO);
			}
		}

		return rooms;
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
		newRoom.setSensorValue(0L);
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
			if (room.getName() == dto.getRoomName()) {
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

	public RoomDTO getUserRoomByName(String email, RoomNameEnum roomName) {
		User user = userService.findByLogin(email);
		Optional<Room> optRoom = repository.findByUserAndName(user, roomName);

		if (optRoom.isPresent()) {
			return parseToDTO(optRoom.get());
		} else {
			return null;
		}

	}

	public void deleteAll() {
		for (Room room : repository.findAll()) {
			repository.delete(room);
		}
	}

	@SuppressWarnings("unchecked")
	public List<RevisionDetailsDTO> getRevisions(String id) {
		AuditQuery auditQuery = auditReader.createQuery().forRevisionsOfEntityWithChanges(Room.class, true)
				.add(AuditEntity.id().eq(id));

		return addKeysToJsonArray(auditQuery.getResultList());
	}
}