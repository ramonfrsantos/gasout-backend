package br.com.gasoutapp.application.dto.notification;

import br.com.gasoutapp.infrastructure.db.entity.notification.Notification;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationGeneratorDTO {
	private String message;
	private String title;
	private String userEmail;

	public NotificationGeneratorDTO(Notification entity) {
		this.message = entity.getMessage();
		this.title = entity.getTitle();
		this.userEmail = entity.getUserEmail();
	}
}