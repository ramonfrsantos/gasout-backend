package br.com.gasoutapp.application.dto.notification;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FirebaseNotificationDTO {
	private List<String> registrationIds;
	private NotificationDTO notification;
}