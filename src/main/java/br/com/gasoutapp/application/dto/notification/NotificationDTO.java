package br.com.gasoutapp.application.dto.notification;

import java.util.Date;

import br.com.gasoutapp.infrastructure.db.entity.notification.Notification;
import lombok.Data;

@Data
public class NotificationDTO {
	private String id;
	private String message;
	private String title;
	private Date date;
	private String userEmail;

	public NotificationDTO() {
	}

	public NotificationDTO(Notification entity) {
		this.id = entity.getId();
		this.message = entity.getMessage();
		this.title = entity.getTitle();
		this.date = entity.getDate();
		this.userEmail = entity.getUserEmail();
	}
}