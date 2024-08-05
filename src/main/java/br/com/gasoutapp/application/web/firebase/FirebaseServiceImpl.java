package br.com.gasoutapp.application.web.firebase;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.application.dto.notification.FirebaseNotificationDTO;
import br.com.gasoutapp.infrastructure.utils.FluentServiceUtils;

@Service
@Slf4j
public class FirebaseServiceImpl implements FirebaseService {

	private static final String FIREBASE_URL = "https://fcm.googleapis.com/fcm/send";

	private final String apiKey;

    public FirebaseServiceImpl(@Value("${firebase.api-key:api_key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
	public void createFirebaseNotification(FirebaseNotificationDTO dto) {
		Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", apiKey);
        headers.put("Content-Type", "application/json");

        try {
            FluentServiceUtils.post(FIREBASE_URL, headers, dto);
        } catch (IOException | URISyntaxException e) {
            log.error("Failed to call firebase API. Message = {}", e.getMessage());
        }
    }
}
