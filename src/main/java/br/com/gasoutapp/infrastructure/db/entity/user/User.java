package br.com.gasoutapp.infrastructure.db.entity.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;
import org.hibernate.envers.AuditJoinTable;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import br.com.gasoutapp.infrastructure.db.entity.enums.UserTypeEnum;
import br.com.gasoutapp.infrastructure.db.entity.notification.Notification;
import br.com.gasoutapp.infrastructure.db.entity.room.Room;
import lombok.Data;

@DynamicUpdate
@Entity
@Data
@Audited(withModifiedFlag = true)
@AuditTable(value = "aud_t_user", catalog = "audit")
@Table(name = "t_user")
@Where(clause = "deleted = false")
public class User {
	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id")
	private String id;

	@Column(name = "name")
	private String name;

	@Column(name = "login")
	private String login;

	@Column(name = "email")
	private String email;

	@Column(name = "password")
	private String password;

	@Column(name = "verification_code")
	private String verificationCode;

	@Column(name = "token_firebase")
	private String tokenFirebase;

	@Column(name = "deleted")
	private boolean deleted;

	@AuditJoinTable(name = "aud_t_user_notification", catalog = "audit")
	@OneToMany
	@Column(name = "fk_notification")
	private List<Notification> notifications = new ArrayList<>();

	@AuditJoinTable(name = "aud_t_user_room", catalog = "audit")
	@OneToMany
	@Column(name = "fk_room")
	private List<Room> rooms = new ArrayList<>();

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "last_update")
	private Date lastUpdate;

	@AuditJoinTable(name = "aud_t_user_role", catalog = "audit")
	@ElementCollection
	@CollectionTable(name = "t_user_role", joinColumns = @JoinColumn(name = "fk_user"))
	@Enumerated(EnumType.STRING)
	@Column(name = "role")
	private List<UserTypeEnum> roles = new ArrayList<>();

}
