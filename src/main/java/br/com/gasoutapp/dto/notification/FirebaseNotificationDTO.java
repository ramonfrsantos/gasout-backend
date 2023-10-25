package br.com.gasoutapp.dto.notification;

import java.util.List;

import lombok.Data;

@Data
public class FirebaseNotificationDTO {
	private List<String> registration_ids;
	private NotificationDTO notification;
}