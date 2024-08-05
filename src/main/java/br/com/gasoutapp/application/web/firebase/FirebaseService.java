package br.com.gasoutapp.application.web.firebase;

import br.com.gasoutapp.application.dto.notification.FirebaseNotificationDTO;

public interface FirebaseService {
	void createFirebaseNotification(FirebaseNotificationDTO dto);
}
