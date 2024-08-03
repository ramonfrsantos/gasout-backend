package br.com.gasoutapp.infrastructure.db.entity.notification;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Date;

@DynamicUpdate
@Entity
@Getter
@Setter
@Audited(withModifiedFlag = true)
@AuditTable(value = "aud_t_notification", catalog = "audit")
@Table(name = "t_notification")
@Where(clause = "deleted = false")
public class Notification {
	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id")
	private String id;

	@Column(name = "title")
	private String title;

	@Column(name = "deleted")
	private boolean deleted;

	@Column(name = "message")
	private String message;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "notification_date")
	private Date date;

	@Column(name = "user")
	private String userEmail;
}