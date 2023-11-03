package br.com.gasoutapp.service.room;

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

import br.com.gasoutapp.domain.enums.RoomNameEnum;
import br.com.gasoutapp.domain.room.Room;
import br.com.gasoutapp.domain.user.User;
import br.com.gasoutapp.dto.audit.RevisionDTO;
import br.com.gasoutapp.dto.room.RoomDTO;
import br.com.gasoutapp.dto.room.RoomNameDTO;
import br.com.gasoutapp.dto.room.RoomSwitchesDTO;
import br.com.gasoutapp.dto.room.SensorDetailsDTO;
import br.com.gasoutapp.exception.AlreadyExistsException;
import br.com.gasoutapp.exception.NotFoundException;
import br.com.gasoutapp.repository.RoomRepository;
import br.com.gasoutapp.service.user.UserService;

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

	public List<RoomDTO> getAllUserRooms(String login, Integer nameId) {
		List<RoomDTO> rooms = new ArrayList<RoomDTO>();

		if (nameId == null || nameId == 0) {
			rooms = parseToDTO(repository.findAllByUserEmail(userService.findByLogin(login).getEmail()));
		} else {
			RoomNameEnum roomName = getRoomNameById(nameId);
			
			RoomDTO roomDTO = getUserRoomByName(login, roomName);
			if (roomDTO != null) {
				rooms.add(roomDTO);
			}
		}

		return rooms;
	}

	public ResponseEntity<RoomDTO> createRoom(RoomNameEnum roomName, String email) {
		List<Room> newUserRooms;
		User newUser;

		User user = userService.findByLogin(email);
		if (user == null) {
			throw new NotFoundException("Usuario nao encontrado.");
		}
		newUser = user;

		List<Room> rooms = repository.findAllByUserEmail(user.getEmail());
		for (Room room : rooms) {
			if (room.getName() == roomName) {
				throw new AlreadyExistsException("Esse cômodo já foi cadastrado.");
			}
		}
		newUserRooms = rooms;

		Room newRoom = new Room();

		newRoom.setName(roomName);
		newRoom.setUserEmail(user.getEmail());
		newRoom.setGasSensorValue(0L);
		newRoom.setUmiditySensorValue(0L);
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
				newRoom.setUmiditySensorValue(dto.getUmiditySensorValue());
				newRoom.setGasSensorValue(dto.getGasSensorValue());
				
				List<Long> gasValues = newRoom.getRecentGasSensorValues();
				
				if(gasValues.size() >= 10) {
					gasValues.remove(0);
					gasValues.add(dto.getGasSensorValue());					
				} else {
					gasValues.add(dto.getGasSensorValue());										
				}
				
				if (dto.getGasSensorValue() <= 0) {
					newRoom.setNotificationOn(false);
					newRoom.setAlarmOn(false);
					newRoom.setSprinklersOn(false);
				} else if (dto.getGasSensorValue() <= 25) {
					newRoom.setNotificationOn(true);
					newRoom.setAlarmOn(false);
					newRoom.setSprinklersOn(false);
				} else if (dto.getGasSensorValue() <= 50) {
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
			throw new NotFoundException("Comodo nao cadastrado.");
		}

	}

	public void deleteAll() {
		for (Room room : repository.findAll()) {
			repository.delete(room);
		}
	}

	public RoomDTO updateSwitches(RoomSwitchesDTO dto) {
		User user = userService.findByLogin(dto.getUserEmail());
		RoomNameEnum roomName = getRoomNameById(dto.getNameId());
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

	public List<Room> findAllByUserEmail(String email) {
		return repository.findAllByUserEmail(email);
	}
}