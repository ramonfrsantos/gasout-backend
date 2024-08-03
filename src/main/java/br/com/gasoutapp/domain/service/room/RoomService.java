package br.com.gasoutapp.domain.service.room;

import java.util.List;
import java.util.Optional;

import br.com.gasoutapp.application.dto.audit.RevisionDTO;
import br.com.gasoutapp.application.dto.room.RoomDTO;
import br.com.gasoutapp.application.dto.room.RoomNameDTO;
import br.com.gasoutapp.application.dto.room.RoomSwitchesDTO;
import br.com.gasoutapp.application.dto.room.SensorDTO;
import br.com.gasoutapp.infrastructure.db.entity.enums.RoomNameEnum;
import br.com.gasoutapp.infrastructure.db.entity.room.Room;

public interface RoomService {

	List<RoomNameDTO> getAllRooms();

	List<RoomDTO> getAllUserRooms(String login, Integer nameId);

	RoomDTO createRoom(RoomNameEnum roomName, String email);

	RoomDTO sendRoomSensorValue(SensorDTO dto);

	String deleteRoom(String id);

	Optional<Room> findRoomById(String id);

	RoomDTO getUserRoomByName(String email, RoomNameEnum roomName);

	void deleteAllByUser(String email);

	RoomDTO updateSwitches(RoomSwitchesDTO dto);

	RoomNameEnum getRoomNameById(Integer id);
	
	List<RevisionDTO> getRevisions(String id);

	List<Room> findAllByUserEmail(String email);

}