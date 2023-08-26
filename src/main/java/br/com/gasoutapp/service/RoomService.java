package br.com.gasoutapp.service;

import static br.com.gasoutapp.utils.JsonUtil.convertToObjectArray;

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
import br.com.gasoutapp.dto.RevisionDTO;
import br.com.gasoutapp.dto.RoomDTO;
import br.com.gasoutapp.dto.RoomNameDTO;
import br.com.gasoutapp.dto.RoomSwitchesDTO;
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
		return Arrays.asList(RoomNameEnum.values()).stream()
				.map(room -> new RoomNameDTO(room.getNameId(), room.getNameDescription())).toList();
	}

	public List<RoomDTO> getAllUserRooms(String login, RoomNameEnum roomName) {
		List<RoomDTO> rooms = new ArrayList<RoomDTO>();

		if (roomName == null) {
			rooms = parseToDTO(repository.findAllByUserEmail(userService.findByLogin(login).getEmail()));
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

		List<Room> rooms = repository.findAllByUserEmail(user.getEmail());
		for (Room room : rooms) {
			if (room.getName().equals(dto.getName())) {
				throw new AlreadyExistsException("Esse cômodo já foi cadastrado.");
			}
		}
		newUserRooms = rooms;

		Room newRoom = new Room();

		newRoom.setName(getRoomNameById(dto.getName().getNameId()));
		newRoom.setUserEmail(user.getEmail());
		newRoom.setSensorValue(0L);
		newRoom.setNotificationOn(false);
		newRoom.setAlarmOn(false);
		newRoom.setSprinklersOn(false);
		newRoom = repository.save(newRoom);

		newUserRooms.add(newRoom);

		userService.setUserRooms(newUserRooms, newUser);

		URI locationRoom = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(newRoom.getId()).toUri();

		return ResponseEntity.created(locationRoom).body(parseToDTO(newRoom));
	}

	public RoomDTO sendRoomSensorValue(SensorDetailsDTO dto) {
		Room newRoom = new Room();

		String login = dto.getUserEmail();

		User user = userService.findByLogin(login);
		if (user == null) {
			throw new NotFoundException("Usuario nao encontrado.");
		}

		List<Room> rooms = repository.findAllByUserEmail(user.getEmail());
		for (Room room : rooms) {
			if (room.getName() == dto.getRoomName()) {
				newRoom = room;
				newRoom.setSensorValue(dto.getSensorValue());

				if (dto.getSensorValue() <= 0) {
					newRoom.setNotificationOn(false);
					newRoom.setAlarmOn(false);
					newRoom.setSprinklersOn(false);
				} else if (dto.getSensorValue() <= 25) {
					newRoom.setNotificationOn(true);
					newRoom.setAlarmOn(false);
					newRoom.setSprinklersOn(false);
				} else if (dto.getSensorValue() <= 50) {
					newRoom.setNotificationOn(true);
					newRoom.setAlarmOn(true);
					newRoom.setSprinklersOn(false);
				} else {
					newRoom.setNotificationOn(true);
					newRoom.setAlarmOn(true);
					newRoom.setSprinklersOn(true);
				}

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
		Optional<Room> optRoom = repository.findByUserEmailAndName(user.getEmail(), roomName);

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

	public RoomDTO updateSwitches(RoomSwitchesDTO dto) {
		User user = userService.findByLogin(dto.getUserEmail());
		RoomNameEnum roomName = getRoomNameById(dto.getRoomNameId());
		Optional<Room> optRoom = repository.findByUserEmailAndName(user.getEmail(), roomName);

		if (optRoom.isEmpty()) {
			throw new NotFoundException("Comodo nao cadastrado.");
		}

		Room room = optRoom.get();

		if (dto.getAlarmOn() != null)
			room.setAlarmOn(dto.getAlarmOn());
		if (dto.getNotificationOn() != null)
			room.setNotificationOn(dto.getNotificationOn());
		if (dto.getSprinklersOn() != null)
			room.setSprinklersOn(dto.getSprinklersOn());

		return parseToDTO(repository.save(room));
	}

	public RoomNameEnum getRoomNameByDescription(String description) {
		for (RoomNameEnum roomEnum : RoomNameEnum.values()) {
			if (roomEnum.getNameDescription().toLowerCase().equals(description.toLowerCase())) {
				return roomEnum;
			}
		}

		throw new NotFoundException("Nao foi encontrado nenhum comodo com esse nome.");
	}

	public RoomNameEnum getRoomNameById(Integer id) {
		for (RoomNameEnum roomEnum : RoomNameEnum.values()) {
			if (roomEnum.getNameId() == id) {
				return roomEnum;
			}
		}

		throw new NotFoundException("Nao foi encontrado nenhum comodo com esse id.");
	}
	
	public List<RevisionDTO> getRevisions(String id) {
		AuditQuery auditQuery = auditReader.createQuery().forRevisionsOfEntityWithChanges(Room.class, true)
				.add(AuditEntity.id().eq(id));

		List<RevisionDTO> details = new ArrayList<RevisionDTO>();

		for (Object revision : auditQuery.getResultList()) {
			RevisionDTO r = new RevisionDTO();

			Object[] objArray = convertToObjectArray(revision);

			r.setEntity(objArray[0]);
			r.setRevisionDetails(objArray[1]);
			r.setRevisionType(objArray[2]);
			r.setUpdatedAttributes(objArray[3]);
			
			details.add(r);
		}

		return details;
	}
}