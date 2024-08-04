package br.com.gasoutapp.domain.service.room;

import br.com.gasoutapp.application.dto.room.RoomDTO;
import br.com.gasoutapp.application.dto.room.RoomNameDTO;
import br.com.gasoutapp.application.dto.room.SensorMinDetailsDTO;
import br.com.gasoutapp.application.dto.user.UserDTO;
import br.com.gasoutapp.domain.service.user.UserService;
import br.com.gasoutapp.infrastructure.db.entity.enums.RoomNameEnum;
import br.com.gasoutapp.infrastructure.db.entity.enums.SensorTypeEnum;
import br.com.gasoutapp.infrastructure.db.entity.room.Room;
import br.com.gasoutapp.infrastructure.db.entity.room.Sensor;
import br.com.gasoutapp.infrastructure.db.entity.user.User;
import br.com.gasoutapp.infrastructure.db.repository.RoomRepository;
import br.com.gasoutapp.infrastructure.db.repository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    String expectedRoomId = "1";
    String expectedUserEmail = "user@test.com";

    @Mock
    Room expectedRoom;

    @Mock
    RoomDTO expectedRoomDTO;

    @Mock
    Sensor expectedSensor;

    @Mock
    User expectedUser;

    @Mock
    RoomRepository roomRepository;

    @Mock
    SensorRepository sensorRepository;

    @Mock
    UserService userService;

    @InjectMocks
    RoomServiceImpl roomService;

    // getRevisions    updateSwitches sendRoomSensorValue deleteRoom deleteAllByUser

    @BeforeEach
    void setUp() {
        expectedRoom = new Room();
        expectedRoom.setId(expectedRoomId);
        expectedRoom.setName(RoomNameEnum.COZINHA);
        expectedRoom.setUserEmail(expectedUserEmail);
        expectedRoom.setDeleted(false);

        expectedUser = new User();
        expectedUser.setId("1");
        expectedUser.setName("User Test");
        expectedUser.setEmail(expectedUserEmail);
        expectedUser.setDeleted(false);
        expectedUser.setRooms(List.of(expectedRoom));

        var expectedUserDTO = new UserDTO();
        expectedUserDTO.setEmail(expectedUserEmail);
        expectedUserDTO.setPassword(expectedUser.getPassword());
        expectedUserDTO.setVerificationCode(expectedUser.getVerificationCode());

        expectedSensor = new Sensor();
        expectedSensor.setId("1");
        expectedSensor.setSensorType(SensorTypeEnum.GAS);
        expectedSensor.setSensorValue(0L);
        expectedSensor.setTimestamp(new Date());

        var expectedRoomNameDTO = new RoomNameDTO();
        expectedRoomNameDTO.setNameId(7);
        expectedRoomNameDTO.setNameDescription("COZINHA");

        expectedRoomDTO = new RoomDTO();
        expectedRoomDTO.setId(expectedRoomId);
        expectedRoomDTO.setGasSensorValue(0L);
        expectedRoomDTO.setUmiditySensorValue(0L);
        expectedRoomDTO.setDetails(expectedRoomNameDTO);
        expectedRoomDTO.setUser(expectedUserDTO);
        expectedRoomDTO.setAlarmOn(false);
        expectedRoomDTO.setNotificationOn(false);
        expectedRoomDTO.setSprinklersOn(false);

        var sensorDetails = new SensorMinDetailsDTO();
        sensorDetails.setSensorValue(0L);
        sensorDetails.setTimestamp(expectedSensor.getTimestamp().toInstant().atZone(ZoneId.of("America/Sao_Paulo")));
        expectedRoomDTO.setRecentGasSensorValues(List.of(sensorDetails));

        expectedSensor.setRoom(expectedRoom);

    }

    @Test
    void getAllRoomsTest(){
        var roomList = roomService.getAllRooms();

        assertNotNull(roomList);
        assertEquals(7, roomList.size());
    }

    @Test
    void findRoomByIdTest(){
        String id = "1";

        when(roomRepository.findById(any())).thenReturn(Optional.of(expectedRoom));

        assertEquals(Optional.of(expectedRoom), roomService.findRoomById(id));

        verify(roomRepository, times(1)).findById(id);
    }

    @Test
    void getAllUserRoomsTest(){
        List<RoomDTO> expectedRoomList = List.of(expectedRoomDTO);

        when(userService.findByLogin(any())).thenReturn(expectedUser);
        when(roomRepository.findByUserEmailAndName(any(), any())).thenReturn(Optional.of(expectedRoom));
        when(sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(any(), any())).thenReturn(List.of(expectedSensor));

        assertEquals(expectedRoomList, roomService.getAllUserRooms(expectedUserEmail, RoomNameEnum.COZINHA.getNameId()));

        verify(roomRepository, times(1)).findByUserEmailAndName(any(), any());
    }

    @Test
    void createRoomTest(){
        when(userService.findByLogin(any())).thenReturn(expectedUser);
        when(roomRepository.findAllByUserEmail(any())).thenReturn(new ArrayList<>());
        when(roomRepository.save(any())).thenReturn(expectedRoom);
        when(sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(any(), any())).thenReturn(List.of(expectedSensor));

        assertEquals(expectedRoomDTO, roomService.createRoom(RoomNameEnum.COZINHA, expectedUserEmail));

        verify(userService, times(1)).findByLogin(any());
        verify(roomRepository, times(1)).findAllByUserEmail(any());
        verify(roomRepository, times(1)).save(any());
        verify(sensorRepository, times(2)).findRecentSensorByRoomOrderByTimestampDesc(any(), any());
    }
}