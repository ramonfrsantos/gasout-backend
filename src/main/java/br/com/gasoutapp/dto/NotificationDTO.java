package br.com.gasoutapp.dto;

import java.util.Date;

import br.com.gasoutapp.domain.Notification;
import lombok.Data;

@Data
public class NotificationDTO {
	private String id;
	private String message;
	private String title;
	private Date date;
	private String email;

	public NotificationDTO() {
	}

	public NotificationDTO(Notification entity) {
		this.id = entity.getId();
		this.message = entity.getMessage();
		this.title = entity.getTitle();
		this.date = entity.getDate();

		if (entity.getUser() != null) {
			this.email = entity.getUser().getEmail();
		}
	}
}