package br.com.gasoutapp.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.dto.FirebaseNotificationDTO;

@Service
public class FirebaseService {

	private final String url = "https://fcm.googleapis.com/fcm/send";

	@Autowired
	private FluentService fluentService;

	public String createFirebaseNotification(FirebaseNotificationDTO dto) throws IOException, URISyntaxException {
		this.fluentService.post(url, Collections.emptyMap(), dto);

		return "Notificação criada com sucesso.";
	}
}
