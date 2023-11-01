package br.com.gasoutapp.domain.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.gasoutapp.domain.enums.UserTypeEnum;
import br.com.gasoutapp.domain.notification.Notification;
import br.com.gasoutapp.domain.room.Room;
import lombok.Data;

@DynamicUpdate
@Entity
@Data
@Audited(withModifiedFlag = true)
@AuditTable(value = "aud_t_user", catalog = "audit")
@Table(name = "t_user")
@Where(clause = "deleted = false")
public class User implements UserDetails {
	private static final long serialVersionUID = 1L;

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

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return getRoles().stream().map(role -> {
			switch (role) {
			case ADMIN:
				return new SimpleGrantedAuthority("ROLE_ADMIN");

			default:
				return new SimpleGrantedAuthority("ROLE_USER");
			}
		}).collect(Collectors.toList());
	}

	@Override
	public String getUsername() {
		return getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
