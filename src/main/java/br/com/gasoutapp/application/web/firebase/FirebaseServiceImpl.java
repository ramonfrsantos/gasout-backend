package br.com.gasoutapp.application.web.firebase;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.application.dto.notification.FirebaseNotificationDTO;
import br.com.gasoutapp.infrastructure.utils.FluentServiceUtils;

@Service
public class FirebaseServiceImpl implements FirebaseService {

	private static final String FIREBASE_URL = "https://fcm.googleapis.com/fcm/send";
	
	@Value("${firebase.api-key}")
	private String apiKey;

	@Autowired
	private FluentServiceUtils fluentService;

	@Override
	public String createFirebaseNotification(FirebaseNotificationDTO dto) throws IOException, URISyntaxException {
		Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", apiKey);
        headers.put("Content-Type", "application/json");
        
		this.fluentService.post(FIREBASE_URL, headers, dto);

		return "Notificação criada com sucesso.";
	}
}
