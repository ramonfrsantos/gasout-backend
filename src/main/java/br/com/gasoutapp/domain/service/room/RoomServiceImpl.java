package br.com.gasoutapp.domain.service.room;

import static br.com.gasoutapp.infrastructure.utils.JsonUtil.convertToObjectArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.application.dto.audit.RevisionDTO;
import br.com.gasoutapp.application.dto.room.RoomDTO;
import br.com.gasoutapp.application.dto.room.RoomNameDTO;
import br.com.gasoutapp.application.dto.room.RoomSwitchesDTO;
import br.com.gasoutapp.application.dto.room.SensorMessageDTO;
import br.com.gasoutapp.domain.exception.AlreadyExistsException;
import br.com.gasoutapp.domain.exception.NotFoundException;
import br.com.gasoutapp.domain.service.user.UserService;
import br.com.gasoutapp.infrastructure.db.entity.enums.RoomNameEnum;
import br.com.gasoutapp.infrastructure.db.entity.room.Room;
import br.com.gasoutapp.infrastructure.db.entity.user.User;
import br.com.gasoutapp.infrastructure.db.repository.RoomRepository;

@Service
public class RoomServiceImpl implements RoomService {

	@Autowired
	private RoomRepository repository;

	@Autowired
	private UserService userService;

	@Autowired
	private AuditReader auditReader;
	
	private static final int GAS_VALUE_LIST_LIMIT_SIZE = 12; 

	@Override
	public List<RoomNameDTO> getAllRooms() {
		return Arrays.asList(RoomNameEnum.values()).stream()
				.map(room -> new RoomNameDTO(room.getNameId(), room.getNameDescription())).toList();
	}

	@Override
	public List<RoomDTO> getAllUserRooms(String login, Integer nameId) {
		List<RoomDTO> rooms = new ArrayList<>();

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

	@Override
	public RoomDTO createRoom(RoomNameEnum roomName, String email) {
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
		
		for(int i=0; i<GAS_VALUE_LIST_LIMIT_SIZE; i++) {
			newRoom.getRecentGasSensorValues().add(0.0);
		}	
		
		newRoom = repository.save(newRoom);

		newUserRooms.add(newRoom);

		userService.setUserRooms(newUserRooms, newUser);

		return parseToDTO(newRoom);
	}

	@Override
	public RoomDTO sendRoomSensorValue(SensorMessageDTO dto) {
		RoomNameEnum roomNameDTO = getRoomNameById(dto.getRoomNameId()); 

		Room newRoom = new Room();
				
		var login = dto.getUserEmail();
		Double gasSensorValue = dto.getGasSensorValue().doubleValue();

		User user = userService.findByLogin(login);
		if (user == null) {
			throw new NotFoundException("Usuario nao encontrado.");
		}

		List<Room> rooms = repository.findAllByUserEmail(user.getEmail());
		for (Room room : rooms) {
			if (room.getName() == roomNameDTO) {
				newRoom = room;
				newRoom.setUmiditySensorValue(dto.getUmiditySensorValue());
				newRoom.setGasSensorValue(dto.getGasSensorValue());
				
				List<Double> gasValues = newRoom.getRecentGasSensorValues();
				
				if(gasValues.size() >= GAS_VALUE_LIST_LIMIT_SIZE) {
					gasValues.remove(0);
					gasValues.add(gasSensorValue);					
				} else {
					gasValues.add(gasSensorValue);										
				}
				
				if (gasSensorValue <= 0) {
					newRoom.setNotificationOn(false);
					newRoom.setAlarmOn(false);
					newRoom.setSprinklersOn(false);
				} else if (gasSensorValue <= 25) {
					newRoom.setNotificationOn(true);
					newRoom.setAlarmOn(false);
					newRoom.setSprinklersOn(false);
				} else if (gasSensorValue <= 50) {
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

	@Override
	public String deleteRoom(String id) {
		Room room = repository.findById(id).orElseThrow(() -> new NotFoundException("Cômodo com o id informado não está cadastrado."));

		room.setDeleted(true);
		repository.save(room);
		
		return "Registro excluido com sucesso.";
	}

	@Override
	public Optional<Room> findRoomById(String id) {
		return repository.findById(id);
	}

	@Override
	public RoomDTO getUserRoomByName(String email, RoomNameEnum roomName) {
		User user = userService.findByLogin(email);
		Optional<Room> optRoom = repository.findByUserEmailAndName(user.getEmail(), roomName);

		if (optRoom.isPresent()) {
			return parseToDTO(optRoom.get());
		} else {
			throw new NotFoundException("Comodo nao cadastrado.");
		}

	}

	@Override
	public void deleteAllByUser(String email) {
		User user = userService.findByLogin(email);
		user.setRooms(null);
		
		for (Room room : repository.findAllByUserEmail(email)) {
			repository.delete(room);
		}
	}

	@Override
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

	@Override
	public RoomNameEnum getRoomNameByDescription(String description) {
		for (RoomNameEnum roomEnum : RoomNameEnum.values()) {
			if (roomEnum.getNameDescription().equalsIgnoreCase(description.toLowerCase())) {
				return roomEnum;
			}
		}

		throw new NotFoundException("Nao foi encontrado nenhum comodo com esse nome.");
	}

	@Override
	public RoomNameEnum getRoomNameById(Integer id) {
		for (RoomNameEnum roomEnum : RoomNameEnum.values()) {
			if (Objects.equals(roomEnum.getNameId(), id)) {
				return roomEnum;
			}
		}

		throw new NotFoundException("Nao foi encontrado nenhum comodo com esse id.");
	}
	
	@Override
	public List<RevisionDTO> getRevisions(String id) {
		AuditQuery auditQuery = auditReader.createQuery().forRevisionsOfEntityWithChanges(Room.class, true)
				.add(AuditEntity.id().eq(id));

		List<RevisionDTO> details = new ArrayList<>();

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

	@Override
	public List<Room> findAllByUserEmail(String email) {
		return repository.findAllByUserEmail(email);
	}
	
	public List<RoomDTO> parseToDTO(List<Room> list) {
		return list.stream().map(this::parseToDTO).toList();
	}

	public Page<RoomDTO> parseToDTO(Page<Room> page) {
		return page.map(RoomDTO::new);
	}

	public RoomDTO parseToDTO(Room room) {
		return new RoomDTO(room);
	}
}