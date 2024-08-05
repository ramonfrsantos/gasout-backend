package br.com.gasoutapp.domain.service.room;

import br.com.gasoutapp.application.dto.room.*;
import br.com.gasoutapp.application.dto.user.UserDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    Date expectedSensorDate;
    String expectedRoomId = "1";
    String expectedUserEmail = "user@test.com";
    Sensor expectedSensor;
    User expectedUser;
    Room expectedRoom;
    RoomDTO expectedRoomDTO;

    @Mock
    RoomRepository roomRepository;

    @Mock
    SensorRepository sensorRepository;

    @Mock
    UserService userService;

    @InjectMocks
    RoomServiceImpl roomService;

    @BeforeEach
    void setUp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2024, Calendar.FEBRUARY, 25);
        expectedSensorDate = calendar.getTime();

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
        expectedSensor.setTimestamp(expectedSensorDate);

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
        sensorDetails.setTimestamp(expectedSensor.getTimestamp().toInstant().atZone(ZoneId.of("America/Sao_Paulo")).withNano(0));
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

        when(userService.findByEmail(any())).thenReturn(expectedUser);
        when(roomRepository.findByUserEmailAndName(any(), any())).thenReturn(Optional.of(expectedRoom));
        when(sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(any(), any())).thenReturn(List.of(expectedSensor));

        assertEquals(expectedRoomList, roomService.getAllUserRooms(expectedUserEmail, RoomNameEnum.COZINHA.getNameId()));

        verify(roomRepository, times(1)).findByUserEmailAndName(any(), any());
        verify(roomRepository, never()).findAllByUserEmail(any());
        verify(userService, times(1)).findByEmail(any());
        verify(sensorRepository, times(2)).findRecentSensorByRoomOrderByTimestampDesc(any(), any());
    }

    @Test
    void getAllUserRoomsThrowsNotFoundException() {
        when(userService.findByEmail(any())).thenReturn(expectedUser);
        when(roomRepository.findByUserEmailAndName(any(), any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, this::invokeGetAllUserRoomsThrowsNotFoundException);
        assertEquals("Comodo nao cadastrado.", ex.getMessage());

        verify(roomRepository, times(1)).findByUserEmailAndName(any(), any());
        verify(roomRepository, never()).findAllByUserEmail(any());
        verify(userService, times(1)).findByEmail(any());
        verify(sensorRepository, never()).findRecentSensorByRoomOrderByTimestampDesc(any(), any());
    }

    private void invokeGetAllUserRoomsThrowsNotFoundException() {
        roomService.getAllUserRooms(expectedUserEmail, RoomNameEnum.COZINHA.getNameId());
    }

    @Test
    void getAllUserRoomsFindByRoomInexistentTest(){
        when(userService.findByEmail(any())).thenReturn(expectedUser);
        when(roomRepository.findAllByUserEmail(any())).thenReturn(List.of(expectedRoom));
        when(sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(any(), any())).thenReturn(List.of(expectedSensor));

        assertEquals(List.of(expectedRoomDTO), roomService.getAllUserRooms(expectedUserEmail, 0));

        verify(roomRepository, times(1)).findAllByUserEmail(any());
        verify(userService, times(1)).findByEmail(any());
    }

    @Test
    void createRoomTest(){
        when(userService.findByEmail(any())).thenReturn(expectedUser);
        when(roomRepository.findAllByUserEmail(any())).thenReturn(new ArrayList<>());
        when(roomRepository.save(any())).thenReturn(expectedRoom);
        when(sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(any(), any())).thenReturn(List.of(expectedSensor));

        assertEquals(expectedRoomDTO, roomService.createRoom(RoomNameEnum.COZINHA, expectedUserEmail));

        verify(userService, times(1)).findByEmail(any());
        verify(roomRepository, times(1)).findAllByUserEmail(any());
        verify(roomRepository, times(1)).save(any());
        verify(sensorRepository, times(2)).findRecentSensorByRoomOrderByTimestampDesc(any(), any());
    }

    @Test
    void createRoomThrowsNotFoundException(){
        when(userService.findByEmail(any())).thenReturn(null);

        var ex = assertThrows(NotFoundException.class, this::invokeCreateRoomThrowsNotFoundException);
        assertEquals("Usuario nao encontrado.", ex.getMessage());

        verify(userService, times(1)).findByEmail(any());
        verify(roomRepository, never()).save(any());
    }

    private void invokeCreateRoomThrowsNotFoundException() {
        roomService.createRoom(RoomNameEnum.COZINHA, "invalid@test.com");
    }

    @Test
    void createRoomThrowsAlreadyExistsException(){
        when(userService.findByEmail(any())).thenReturn(expectedUser);
        when(roomRepository.findAllByUserEmail(any())).thenReturn(List.of(expectedRoom));

        var ex = assertThrows(AlreadyExistsException.class, this::invokeCreateRoomThrowsAlreadyExistsException);
        assertEquals("Esse cômodo já foi cadastrado.", ex.getMessage());

        verify(userService, times(1)).findByEmail(any());
        verify(roomRepository, times(1)).findAllByUserEmail(any());
        verify(roomRepository, never()).save(any());
    }

    private void invokeCreateRoomThrowsAlreadyExistsException() {
        roomService.createRoom(RoomNameEnum.COZINHA, expectedUserEmail);
    }

    @Test
    void updateSwitchesTest(){
        RoomSwitchesDTO switches = new RoomSwitchesDTO();
        switches.setAlarmOn(true);
        switches.setSprinklersOn(false);
        switches.setNotificationOn(true);
        switches.setUserEmail(expectedUserEmail);
        switches.setNameId(RoomNameEnum.COZINHA.getNameId());

        when(userService.findByEmail(any())).thenReturn(expectedUser);
        when(roomRepository.findByUserEmailAndName(any(), any())).thenReturn(Optional.of(expectedRoom));
        when(roomRepository.save(any())).thenReturn(expectedRoom);
        when(sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(any(), any())).thenReturn(List.of(expectedSensor));

        var result = roomService.updateSwitches(switches);

        assertNotNull(result);
        assertTrue(result.getAlarmOn());
        assertFalse(result.getSprinklersOn());
        assertTrue(result.getNotificationOn());

        verify(userService, times(1)).findByEmail(any());
        verify(roomRepository, times(1)).findByUserEmailAndName(any(), any());
        verify(roomRepository, times(1)).save(any());
        verify(sensorRepository, times(2)).findRecentSensorByRoomOrderByTimestampDesc(any(), any());
    }

    @Test
    void updateSwitchesThrowsNotFoundException(){
        when(userService.findByEmail(any())).thenReturn(expectedUser);
        when(roomRepository.findByUserEmailAndName(any(), any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, this::invokeUpdateSwitchesThrowsNotFoundException);
        assertEquals("Comodo nao cadastrado.", ex.getMessage());

        verify(userService, times(1)).findByEmail(any());
        verify(roomRepository, times(1)).findByUserEmailAndName(any(), any());
        verify(roomRepository, never()).save(any());
        verify(sensorRepository, never()).findRecentSensorByRoomOrderByTimestampDesc(any(), any());
    }

    private void invokeUpdateSwitchesThrowsNotFoundException() {
        RoomSwitchesDTO switches = new RoomSwitchesDTO();
        switches.setAlarmOn(true);
        switches.setSprinklersOn(false);
        switches.setNotificationOn(true);
        switches.setUserEmail(expectedUserEmail);
        switches.setNameId(RoomNameEnum.COZINHA.getNameId());

        roomService.updateSwitches(switches);
    }

    @ParameterizedTest
    @CsvSource({"0", "25", "50", "90"})
    void sendRoomSensorValueTest(long sensorValue){
        var sensor = new SensorDTO();
        sensor.setSensorValue(sensorValue);
        sensor.setSensorType(SensorTypeEnum.GAS);
        sensor.setRoomNameId(RoomNameEnum.COZINHA.getNameId());
        sensor.setTimestamp(new Date());
        sensor.setUserEmail(expectedUserEmail);

        when(userService.findByEmail(any())).thenReturn(expectedUser);
        when(roomRepository.findAllByUserEmail(any())).thenReturn(List.of(expectedRoom));
        when(sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(any(), any())).thenReturn(List.of(expectedSensor));

        var timestamp = new Date().toInstant().atZone(ZoneId.of("America/Sao_Paulo")).withNano(0);

        RoomDTO updatedRoom = expectedRoomDTO;
        updatedRoom.getRecentGasSensorValues().get(0).setTimestamp(timestamp);
        updatedRoom.setGasSensorValue(sensorValue);
        updatedRoom.setUmiditySensorValue(sensorValue);

        var result = roomService.sendRoomSensorValue(sensor);

        assertEquals(updatedRoom.getGasSensorValue(), result.getGasSensorValue());
        assertEquals(updatedRoom.getUmiditySensorValue(),result.getUmiditySensorValue());
        assertEquals(updatedRoom.getUser(), result.getUser());
        assertEquals(updatedRoom.getDetails(), result.getDetails());

        verify(userService, times(1)).findByEmail(any());
        verify(roomRepository, times(1)).findAllByUserEmail(any());
        verify(roomRepository, times(1)).save(any());
        verify(sensorRepository, times(3)).findRecentSensorByRoomOrderByTimestampDesc(any(), any());
        verify(sensorRepository, never()).findOldestSensorByRoomOrderByTimestampAsc(any(), any());
        verify(sensorRepository, times(1)).save(any());
    }

    @Test
    void sendRoomSensorValueWithSameTimestampTest(){
        var sensor = new SensorDTO();
        sensor.setSensorValue(0L);
        sensor.setSensorType(SensorTypeEnum.GAS);
        sensor.setRoomNameId(RoomNameEnum.COZINHA.getNameId());
        sensor.setTimestamp(new Date());
        sensor.setUserEmail(expectedUserEmail);

        expectedSensor.setTimestamp(new Date());

        when(userService.findByEmail(any())).thenReturn(expectedUser);
        when(roomRepository.findAllByUserEmail(any())).thenReturn(List.of(expectedRoom));
        when(sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(any(), any())).thenReturn(getFilledsensorList());
        when(sensorRepository.findOldestSensorByRoomOrderByTimestampAsc(any(), any())).thenReturn(getFilledsensorList());

        var updatedRoom = roomService.sendRoomSensorValue(sensor);
        assertEquals(12, updatedRoom.getRecentGasSensorValues().size());

        verify(userService, times(1)).findByEmail(any());
        verify(roomRepository, times(1)).findAllByUserEmail(any());
        verify(roomRepository, times(1)).save(any());
        verify(sensorRepository, times(3)).findRecentSensorByRoomOrderByTimestampDesc(any(), any());
        verify(sensorRepository, times(1)).findOldestSensorByRoomOrderByTimestampAsc(any(), any());
        verify(sensorRepository, times(1)).save(any());
    }

    @Test
    void sendRoomSensorValueThrowsListSizeNotValidException(){
        expectedSensor.setTimestamp(new Date());

        when(userService.findByEmail(any())).thenReturn(expectedUser);
        when(roomRepository.findAllByUserEmail(any())).thenReturn(List.of(expectedRoom));
        when(sensorRepository.findRecentSensorByRoomOrderByTimestampDesc(any(), any())).thenReturn(List.of(expectedSensor));
        when(sensorRepository.findOldestSensorByRoomOrderByTimestampAsc(any(), any())).thenReturn(List.of(expectedSensor));

        var ex = assertThrows(ListSizeNotValidException.class, this::invokeSendRoomSensorValueThrowsListSizeNotValidException);
        assertEquals("A lista de valores possui tamanho invalido.", ex.getMessage());

        verify(userService, times(1)).findByEmail(any());
        verify(roomRepository, times(1)).findAllByUserEmail(any());
        verify(roomRepository, never()).save(any());
        verify(sensorRepository, times(1)).findRecentSensorByRoomOrderByTimestampDesc(any(), any());
        verify(sensorRepository, times(1)).findOldestSensorByRoomOrderByTimestampAsc(any(), any());
        verify(sensorRepository, never()).save(any());
    }

    private void invokeSendRoomSensorValueThrowsListSizeNotValidException() {
        var sensor = new SensorDTO();
        sensor.setSensorValue(0L);
        sensor.setSensorType(SensorTypeEnum.GAS);
        sensor.setRoomNameId(RoomNameEnum.COZINHA.getNameId());
        sensor.setTimestamp(new Date());
        sensor.setUserEmail(expectedUserEmail);

        roomService.sendRoomSensorValue(sensor);
    }

    @Test
    void deleteRoomTest(){
        when(roomRepository.findById(any())).thenReturn(Optional.of(expectedRoom));

        assertEquals("Registro excluido com sucesso.", roomService.deleteRoom(expectedRoomId));
        assertTrue(expectedRoom.isDeleted());

        verify(roomRepository, times(1)).findById(any());
        verify(roomRepository, times(1)).save(any());
    }

    @Test
    void getRoomNameByIdTest() {
        assertEquals(RoomNameEnum.COZINHA, roomService.getRoomNameById(7));
    }

    @Test
    void findAllByUserEmailTest() {
        when(roomRepository.findAllByUserEmail(any())).thenReturn(List.of(expectedRoom));

        assertEquals(List.of(expectedRoom), roomService.findAllByUserEmail(expectedUserEmail));

        verify(roomRepository, times(1)).findAllByUserEmail(any());
    }

    @Test
    void getRoomNameByIdThrowsNotFoundException() {
        var ex = assertThrows(NotFoundException.class, this::invokeGetRoomNameByIdThrowsNotFoundException);
        assertEquals("Nao foi encontrado nenhum comodo com esse id.", ex.getMessage());
    }

    private void invokeGetRoomNameByIdThrowsNotFoundException() {
        roomService.getRoomNameById(0);
    }

    @Test
    void deleteAllByUserTest(){
        when(userService.findByEmail(any())).thenReturn(expectedUser);
        when(roomRepository.findAllByUserEmail(any())).thenReturn(List.of(expectedRoom));
        when(sensorRepository.findAllByRoom(any())).thenReturn(List.of(expectedSensor));

        roomService.deleteAllByUser(expectedUserEmail);

        assertNull(expectedUser.getRooms());

        verify(userService, times(1)).findByEmail(any());
        verify(roomRepository, times(1)).delete(any());
        verify(sensorRepository, times(1)).delete(any());
    }

    private List<Sensor> getFilledsensorList() {
        return List.of(
                expectedSensor,
                expectedSensor,
                expectedSensor,
                expectedSensor,
                expectedSensor,
                expectedSensor,
                expectedSensor,
                expectedSensor,
                expectedSensor,
                expectedSensor,
                expectedSensor,
                expectedSensor
        );
    }

}