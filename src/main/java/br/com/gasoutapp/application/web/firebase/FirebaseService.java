package br.com.gasoutapp.application.web.firebase;

import java.io.IOException;
import java.net.URISyntaxException;

import br.com.gasoutapp.application.dto.notification.FirebaseNotificationDTO;

public interface FirebaseService {
	void createFirebaseNotification(FirebaseNotificationDTO dto) throws IOException, URISyntaxException;
}
