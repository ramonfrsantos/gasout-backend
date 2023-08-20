package br.com.gasoutapp.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.dto.FirebaseNotificationDTO;

@Service
public class FirebaseService {

	private final String url = "https://fcm.googleapis.com/fcm/send";
	
	@Value("${firebase.api-key}")
	private String API_KEY;

	@Autowired
	private FluentService fluentService;

	public String createFirebaseNotification(FirebaseNotificationDTO dto) throws IOException, URISyntaxException {
		Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", API_KEY);
        headers.put("Content-Type", "application/json");
        
		this.fluentService.post(url, headers, dto);

		return "Notificação criada com sucesso.";
	}
}
