package br.com.gasoutapp.domain.service.room;

import java.util.List;
import java.util.Optional;

import br.com.gasoutapp.application.dto.audit.RevisionDTO;
import br.com.gasoutapp.application.dto.room.RoomDTO;
import br.com.gasoutapp.application.dto.room.RoomNameDTO;
import br.com.gasoutapp.application.dto.room.RoomSwitchesDTO;
import br.com.gasoutapp.application.dto.room.SensorMessageDTO;
import br.com.gasoutapp.domain.entity.enums.RoomNameEnum;
import br.com.gasoutapp.domain.entity.room.Room;

public interface RoomService {

	public List<RoomNameDTO> getAllRooms();

	public List<RoomDTO> getAllUserRooms(String login, Integer nameId);

	public RoomDTO createRoom(RoomNameEnum roomName, String email);

	public RoomDTO sendRoomSensorValue(SensorMessageDTO dto);

	public String deleteRoom(String id);

	public Optional<Room> findRoomById(String id);

	public RoomDTO getUserRoomByName(String email, RoomNameEnum roomName);

	public void deleteAllByUser(String email);

	public RoomDTO updateSwitches(RoomSwitchesDTO dto);

	public RoomNameEnum getRoomNameByDescription(String description);

	public RoomNameEnum getRoomNameById(Integer id);
	
	public List<RevisionDTO> getRevisions(String id);

	public List<Room> findAllByUserEmail(String email);
}