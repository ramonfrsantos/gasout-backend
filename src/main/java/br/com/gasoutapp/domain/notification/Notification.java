package br.com.gasoutapp.domain.notification;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import lombok.Data;

@DynamicUpdate
@Entity
@Data
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