package br.com.gasoutapp.domain.service.room;

import static br.com.gasoutapp.infrastructure.utils.DateUtils.differenceInMinutes;
import static br.com.gasoutapp.infrastructure.utils.JsonUtil.convertToObjectArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;

import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.application.dto.audit.RevisionDTO;
import br.com.gasoutapp.application.dto.room.RoomDTO;
import br.com.gasoutapp.application.dto.room.RoomNameDTO;
import br.com.gasoutapp.application.dto.room.RoomSwitchesDTO;
import br.com.gasoutapp.application.dto.room.SensorDTO;
import br.com.gasoutapp.application.dto.room.SensorMinDetailsDTO;
import br.com.gasoutapp.domain.exception.AlreadyExistsException;
import br.com.gasoutapp.domain.exception.ListSizeNotValidException;
import br.com.gasoutapp.domain.exception.NotFoundException;
import br.com.gasoutapp.domain.service.user.UserService;
import br.com.gasoutapp.infrastructure.db.entity.enums.RoomNameEnum;
import br.com.gasoutapp.infrastructure.db.entity.enums.SensorTypeEnum;
import br.com.gasoutapp.infrastructure.db.entity.room.Room;
import br.com.gasoutapp.infrastructure.db.entity.room.Sensor;
import br.com.gasoutapp.infrastructure.db.entity.user.User;
import br.com.gasoutapp.infrastructure.db.repository.RoomRepository;
import br.com.gasoutapp.infrastructure.db.repository.SensorRepository;

@Service
public class RoomServiceImpl implements RoomService {

	@Autowired
	private RoomRepository repository;

	@Autowired
	private SensorRepository sensorRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private EntityManagerFactory factory;

	private static final int GAS_VALUE_LIST_LIMIT_SIZE = 12;
	private static final int SENSOR_MEASUREMENT_DELAY_IN_MINUTES = 0;

    @Override
	public List<RoomNameDTO> getAllRooms() {
		return Arrays.stream(RoomNameEnum.values())
				.map(room -> new RoomNameDTO(room.getNameId(), room.getNameDescription())).toList();
	}

	@Override
	public List<RoomDTO> getAllUserRooms(String login, Integer nameId) {
		List<RoomDTO> rooms = new ArrayList<>();

		if (nameId == null || nameId == 0) {
			rooms = parseToDTO(repository.findAllByUserEmail(userService.findByLogin(login).getEmail()));
		} else {
			var roomName = getRoomNameById(nameId);

			var roomDTO = getUserRoomByName(login, roomName);
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

		var user = userService.findByLogin(email);
		if (user == null) {
			throw new NotFoundException("Usuario nao encontrado.");
		}
		newUser = user;

		var rooms = repository.findAllByUserEmail(user.getEmail());
		for (Room room : rooms) {
			if (room.getName() == roomName) {
				throw new AlreadyExistsException("Esse cômodo já foi cadastrado.");
			}
		}
		newUserRooms = rooms;

		var newRoom = new Room();
		newRoom.setName(roomName);
		newRoom.setUserEmail(user.getEmail());
		newRoom.setNotificationOn(false);
		newRoom.setAlarmOn(false);
		newRoom.setSprinklersOn(false);
		newRoom = repository.save(newRoom);

		createSensors(newRoom);

		newUserRooms.add(newRoom);

		userService.setUserRooms(newUserRooms, newUser);

		return parseToDTO(newRoom,
				sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(newRoom, SensorTypeEnum.GAS),
				sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(newRoom, SensorTypeEnum.UMIDADE));
	}

	@Transactional
	@Override
	public RoomDTO sendRoomSensorValue(SensorDTO dto) {
		var user = userService.findByEmail(dto.getUserEmail());
		var roomNameDTO = getRoomNameById(dto.getRoomNameId());
		var rooms = repository.findAllByUserEmail(user.getEmail());
		
		var newRoom = new Room();

		for (Room room : rooms) {
			if (room.getName() == roomNameDTO) {
				newRoom = room;

				var newSensor = new Sensor();
				var mostRecentSensor = sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(newRoom, dto.getSensorType()).get(0);

				if (differenceInMinutes(new Date(),
						mostRecentSensor.getTimestamp()) < SENSOR_MEASUREMENT_DELAY_IN_MINUTES) {
					newSensor = mostRecentSensor;
				} else {
					deleteOldestSensorValue(newRoom, dto.getSensorType());
				}
				
				newSensor.setRoom(newRoom);
				newSensor.setSensorType(dto.getSensorType());
				newSensor.setSensorValue(dto.getSensorValue());
				newSensor.setTimestamp(new Date());
				sensorRepository.save(newSensor);

				if (dto.getSensorType() == SensorTypeEnum.GAS) {
					updateRoomStateBasedOnGasSensorValue(newRoom, dto.getSensorValue());
				}
			}
		}

		return parseToDTO(newRoom,
				sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(newRoom, SensorTypeEnum.GAS),
				sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(newRoom, SensorTypeEnum.UMIDADE));
	}
	
	private void updateRoomStateBasedOnGasSensorValue(Room room, Long gasSensorValue) {
	    if (gasSensorValue <= 0) {
	        room.setNotificationOn(false);
	        room.setAlarmOn(false);
	        room.setSprinklersOn(false);
	    } else if (gasSensorValue <= 25) {
	        room.setNotificationOn(true);
	        room.setAlarmOn(false);
	        room.setSprinklersOn(false);
	    } else if (gasSensorValue <= 50) {
	        room.setNotificationOn(true);
	        room.setAlarmOn(true);
	        room.setSprinklersOn(false);
	    } else {
	        room.setNotificationOn(true);
	        room.setAlarmOn(true);
	        room.setSprinklersOn(true);
	    }
	    
		repository.save(room);
	}

	@Override
	public String deleteRoom(String id) {
		var room = repository.findById(id)
				.orElseThrow(() -> new NotFoundException("Cômodo com o id informado não está cadastrado."));

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
		var user = userService.findByLogin(email);
		var optRoom = repository.findByUserEmailAndName(user.getEmail(), roomName);

		if (optRoom.isPresent()) {
			return parseToDTO(optRoom.get(),
					sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(optRoom.get(), SensorTypeEnum.GAS),
					sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(optRoom.get(), SensorTypeEnum.UMIDADE));
		} else {
			throw new NotFoundException("Comodo nao cadastrado.");
		}

	}

	@Override
	public void deleteAllByUser(String email) {
		var user = userService.findByLogin(email);
		user.setRooms(null);

		for (Room room : repository.findAllByUserEmail(email)) {
			for (Sensor sensor : sensorRepository.findAllByRoom(room))
				sensorRepository.delete(sensor);

			repository.delete(room);
		}
	}

	@Override
	public RoomDTO updateSwitches(RoomSwitchesDTO dto) {
		var user = userService.findByLogin(dto.getUserEmail());
		var roomName = getRoomNameById(dto.getNameId());
		var optRoom = repository.findByUserEmailAndName(user.getEmail(), roomName);

		if (optRoom.isEmpty()) {
			throw new NotFoundException("Comodo nao cadastrado.");
		}

		var room = optRoom.get();

		if (dto.getAlarmOn() != null)
			room.setAlarmOn(dto.getAlarmOn());
		if (dto.getNotificationOn() != null)
			room.setNotificationOn(dto.getNotificationOn());
		if (dto.getSprinklersOn() != null)
			room.setSprinklersOn(dto.getSprinklersOn());

		var newRoom = repository.save(room);

		return parseToDTO(newRoom,
				sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(newRoom, SensorTypeEnum.GAS),
				sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(room, SensorTypeEnum.UMIDADE));
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
		var auditReader = AuditReaderFactory.get(factory.createEntityManager());

		var auditQuery = auditReader.createQuery().forRevisionsOfEntityWithChanges(Room.class, true)
				.add(AuditEntity.id().eq(id));

		List<RevisionDTO> details = new ArrayList<>();

		for (Object revision : auditQuery.getResultList()) {
			var r = new RevisionDTO();

			var objArray = convertToObjectArray(revision);
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
	
	private void deleteOldestSensorValue(Room room, SensorTypeEnum sensorType) {
		var sensors = sensorRepository.findOldestSensorByRoomOrderByTimestampAsc(room, sensorType);
		if(sensors.size() == GAS_VALUE_LIST_LIMIT_SIZE) {
			sensorRepository.delete(sensors.get(0));			
		} else {
			throw new ListSizeNotValidException("A lista de valores possui tamanho invalido.");
		}
	}

	private void createSensors(Room room) {
		for (int i = 0; i < GAS_VALUE_LIST_LIMIT_SIZE; i++) {
			var dateNow = new Date();

			var sensorGas = new Sensor();
			sensorGas.setRoom(room);
			sensorGas.setSensorType(SensorTypeEnum.GAS);
			sensorGas.setSensorValue(0L);
			sensorGas.setTimestamp(dateNow);

			sensorRepository.save(sensorGas);

			var sensorUmidity = new Sensor();
			sensorUmidity.setRoom(room);
			sensorUmidity.setSensorType(SensorTypeEnum.UMIDADE);
			sensorUmidity.setSensorValue(0L);
			sensorUmidity.setTimestamp(dateNow);

			sensorRepository.save(sensorUmidity);
		}
	}

	public List<RoomDTO> parseToDTO(List<Room> list) {
		return list.stream()
				.map(room -> parseToDTO(room,
						sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(room, SensorTypeEnum.GAS),
						sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(room, SensorTypeEnum.UMIDADE)))
				.toList();
	}


	public RoomDTO parseToDTO(Room room, List<Sensor> recentGasSensorValues, List<Sensor> recentUmiditySensorValues) {
		var roomDTO = new RoomDTO(room);
		roomDTO.setRecentGasSensorValues(recentGasSensorValues.stream().map(SensorMinDetailsDTO::new).toList());
		roomDTO.setGasSensorValue(recentGasSensorValues.get(0).getSensorValue());
		roomDTO.setUmiditySensorValue(recentUmiditySensorValues.get(0).getSensorValue());

		return roomDTO;
	}

}