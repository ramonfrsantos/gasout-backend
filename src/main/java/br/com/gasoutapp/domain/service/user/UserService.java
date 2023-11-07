package br.com.gasoutapp.domain.service.user;

import java.util.List;
import java.util.Optional;

import br.com.gasoutapp.application.dto.audit.RevisionDTO;
import br.com.gasoutapp.application.dto.user.LoginDTO;
import br.com.gasoutapp.application.dto.user.UserDTO;
import br.com.gasoutapp.infrastructure.config.security.LoginResultDTO;
import br.com.gasoutapp.infrastructure.db.entity.enums.UserTypeEnum;
import br.com.gasoutapp.infrastructure.db.entity.notification.Notification;
import br.com.gasoutapp.infrastructure.db.entity.room.Room;
import br.com.gasoutapp.infrastructure.db.entity.user.User;

public interface UserService {

	public UserDTO register(UserDTO userDTO);

	public User create(UserDTO userDTO);

	public String delete(String login);

	public List<UserDTO> findAll();

	public String getVerificationCode(String login);

	public boolean checkIfCodesAreEqual(String login, String newCode);

	public String sendVerificationMail(String login);

	public UserDTO refreshPassword(LoginDTO dto);

	public Optional<User> findUserById(String id);

	public List<User> findAllByRoles(UserTypeEnum userType);

	public LoginResultDTO getDtoByUser(User user, String tokenFirebase);

	public User findByLogin(String login);

	public User findByLoginAndPassword(String login, String password);

	public User findByEmail(String email);

	public void setUserRooms(List<Room> newUserRooms, User user);

	public void setUserNotifications(List<Notification> newUserNotifications, User user);

	public List<RevisionDTO> getRevisions(String id);
}