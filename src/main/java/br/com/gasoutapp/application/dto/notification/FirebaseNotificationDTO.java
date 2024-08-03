package br.com.gasoutapp.application.dto.notification;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FirebaseNotificationDTO {
	private List<String> registration_ids;
	private NotificationDTO notification;
}