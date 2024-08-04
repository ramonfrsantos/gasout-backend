package br.com.gasoutapp.application.dto.notification;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationGeneratorDTO {
	private String message;
	private String title;
	private String userEmail;
}