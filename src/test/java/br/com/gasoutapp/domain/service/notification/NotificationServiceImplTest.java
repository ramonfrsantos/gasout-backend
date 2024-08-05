package br.com.gasoutapp.domain.service.notification;

import br.com.gasoutapp.application.dto.notification.NotificationDTO;
import br.com.gasoutapp.application.dto.room.SensorDTO;
import br.com.gasoutapp.application.dto.room.SensorGasPayloadDTO;
import br.com.gasoutapp.application.web.firebase.FirebaseService;
import br.com.gasoutapp.domain.exception.NotFoundException;
import br.com.gasoutapp.domain.service.room.RoomService;
import br.com.gasoutapp.domain.service.user.UserService;
import br.com.gasoutapp.infrastructure.db.entity.enums.RoomNameEnum;
import br.com.gasoutapp.infrastructure.db.entity.enums.SensorTypeEnum;
import br.com.gasoutapp.infrastructure.db.entity.notification.Notification;
import br.com.gasoutapp.infrastructure.db.entity.room.Room;
import br.com.gasoutapp.infrastructure.db.entity.user.User;
import br.com.gasoutapp.infrastructure.db.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    String expectedUserEmail = "user@test.com";
    Notification expectedNotification;
    NotificationDTO expectedNotificationDTO;
    Room expectedRoom;

    @Mock
    UserService userService;

    @Mock
    RoomService roomService;

    @Mock
    FirebaseService firebaseService;

    @Mock
    NotificationRepository notificationRepository;

    @InjectMocks
    NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        expectedRoom = new Room();
        expectedRoom.setId("1");
        expectedRoom.setName(RoomNameEnum.COZINHA);
        expectedRoom.setUserEmail(expectedUserEmail);
        expectedRoom.setNotificationOn(true);
        expectedRoom.setDeleted(false);

        expectedNotification = new Notification();
        expectedNotification.setId("1");
        expectedNotification.setTitle("Title Test");
        expectedNotification.setMessage("Message test");
        expectedNotification.setUserEmail(expectedUserEmail);
        expectedNotification.setDeleted(false);
        expectedNotification.setDate(new Date());

        expectedNotificationDTO = new NotificationDTO(expectedNotification);
    }

    @Test
    void getAllNotificationsTest(){
        var expectedNotificationList = List.of(expectedNotificationDTO);

        when(notificationRepository.findAll()).thenReturn(List.of(expectedNotification));

        assertEquals(expectedNotificationList, notificationService.getAllNotifications());

        verify(notificationRepository, times(1)).findAll();
    }

    @Test
    void getAllRecentNotificationsTest(){
        var expectedNotificationList = List.of(expectedNotificationDTO);

        when(userService.findByEmail(any())).thenReturn(new User());
        when(notificationRepository.findAllByUserEmailOrderByDateAsc(any())).thenReturn(List.of(expectedNotification));

        assertEquals(expectedNotificationList, notificationService.getAllRecentNotifications(expectedUserEmail));

        verify(userService, times(1)).findByEmail(any());
        verify(notificationRepository, times(1)).findAllByUserEmailOrderByDateAsc(any());
    }

    @Test
    void findNotificationByIdTest(){
        when(notificationRepository.findById(any())).thenReturn(Optional.of(expectedNotification));

        assertEquals(Optional.of(expectedNotification), notificationService.findNotificationById("1"));

        verify(notificationRepository, times(1)).findById(any());
    }

    @Test
    void createNotificationTest(){
        when(userService.findByEmail(any())).thenReturn(new User());
        when(notificationRepository.findAllByUserEmailOrderByDateAsc(any())).thenReturn(new ArrayList<>());
        when(notificationRepository.save(any())).thenReturn(expectedNotification);

        assertEquals(expectedNotificationDTO, notificationService.createNotification(expectedNotificationDTO));

        verify(userService, times(1)).findByEmail(any());
        verify(notificationRepository, times(2)).findAllByUserEmailOrderByDateAsc(any());
        verify(notificationRepository, times(2)).save(any());
    }

    @Test
    void createNotificationListHasMoreThanTenNotificationsTest(){
        when(userService.findByEmail(any())).thenReturn(new User());
        when(notificationRepository.findAllByUserEmailOrderByDateAsc(any())).thenReturn(getNotificationListFullFilled());
        when(notificationRepository.save(any())).thenReturn(expectedNotification);

        var updatedNotificationDTO = expectedNotificationDTO;
        updatedNotificationDTO.setUserEmail(null);

        assertEquals(updatedNotificationDTO, notificationService.createNotification(expectedNotificationDTO));

        verify(userService, times(1)).findByEmail(any());
        verify(notificationRepository, times(2)).findAllByUserEmailOrderByDateAsc(any());
        verify(userService, times(2)).setUserNotifications(any(), any());
        verify(notificationRepository, times(21)).save(any());
    }

    @Test
    void createNotificationThrowsNotFoundException(){
        when(userService.findByEmail(any())).thenReturn(null);

        var ex = assertThrows(NotFoundException.class, this::invokeCreateNotificationThrowsNotFoundException);
        assertEquals("Usuario nao encontrado.", ex.getMessage());

        verify(userService, times(1)).findByEmail(any());
        verify(notificationRepository, never()).findAllByUserEmailOrderByDateAsc(any());
        verify(notificationRepository, never()).save(any());
    }

    private void invokeCreateNotificationThrowsNotFoundException() {
        notificationService.createNotification(expectedNotificationDTO);
    }

    @Test
    void deleteNotificationTest(){
        when(notificationRepository.findById(any())).thenReturn(Optional.of(expectedNotification));

        assertEquals("Registro excluido com sucesso.", notificationService.deleteNotification("1"));
        assertTrue(expectedNotification.isDeleted());

        verify(notificationRepository, times(1)).findById(any());
        verify(notificationRepository, times(1)).save(any());
    }

    @ParameterizedTest
    @CsvSource({"0, GAS", "25, GAS", "50, GAS", "90, GAS", "90, UMIDADE"})
    void sendPushTest(long sensorValue, SensorTypeEnum sensorType) {
        firebaseService = mock(FirebaseService.class);

        var sensor = new SensorDTO();
        sensor.setTimestamp(new Date());
        sensor.setSensorType(sensorType);
        sensor.setSensorValue(sensorValue);
        sensor.setRoomNameId(RoomNameEnum.COZINHA.getNameId());

        var payloadDTO = new SensorGasPayloadDTO();
        payloadDTO.setSensors(List.of(sensor));

        var user = new User();
        user.setTokenFirebase("VyIWGTYVFGn2AA8ozdqnuQ==");

        when(roomService.getRoomNameById(any())).thenReturn(RoomNameEnum.COZINHA);
        when(roomService.findAllByUserEmail(any())).thenReturn(List.of(expectedRoom));
        when(userService.findByEmail(any())).thenReturn(user);

        if(sensorType.equals(SensorTypeEnum.GAS)){
            when(notificationRepository.findAllByUserEmailOrderByDateAsc(any())).thenReturn(new ArrayList<>());
            when(notificationRepository.save(any())).thenReturn(expectedNotification);

            assertTrue(notificationService.sendPush(payloadDTO).getNotificationCreated());
        } else {
            assertFalse(notificationService.sendPush(payloadDTO).getNotificationCreated());
        }

    }

    private List<Notification> getNotificationListFullFilled() {
        List<Notification> list = new ArrayList<>();
        list.add(expectedNotification);
        list.add(expectedNotification);
        list.add(expectedNotification);
        list.add(expectedNotification);
        list.add(expectedNotification);
        list.add(expectedNotification);
        list.add(expectedNotification);
        list.add(expectedNotification);
        list.add(expectedNotification);
        list.add(expectedNotification);
        return  list;
    }
}