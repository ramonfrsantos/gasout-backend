package br.com.gasoutapp.application.web.firebase;

import br.com.gasoutapp.application.dto.notification.FirebaseNotificationDTO;
import br.com.gasoutapp.application.dto.notification.NotificationDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FirebaseServiceImplTest {
    @Mock
    private FirebaseServiceImpl firebaseServiceImpl;

    @Test
    void createFirebaseNotificationTest() {
        firebaseServiceImpl = new FirebaseServiceImpl("");
        var dto = new FirebaseNotificationDTO();
        dto.setNotification(new NotificationDTO());
        dto.setRegistrationIds(List.of("", ""));

        assertDoesNotThrow(() -> firebaseServiceImpl.createFirebaseNotification(dto));
    }

}