package br.com.gasoutapp.domain.service.user;

import java.util.List;
import java.util.Optional;

import br.com.gasoutapp.application.dto.user.LoginDTO;
import br.com.gasoutapp.application.dto.user.UserDTO;
import br.com.gasoutapp.application.dto.LoginResultDTO;
import br.com.gasoutapp.infrastructure.db.entity.enums.UserTypeEnum;
import br.com.gasoutapp.infrastructure.db.entity.notification.Notification;
import br.com.gasoutapp.infrastructure.db.entity.room.Room;
import br.com.gasoutapp.infrastructure.db.entity.user.User;

public interface UserService {

	UserDTO register(UserDTO userDTO);

	User create(UserDTO userDTO);

	String delete(String login);

	List<UserDTO> findAll();

	String getVerificationCode(String login);

	boolean checkIfCodesAreEqual(String login, String newCode);

	String sendVerificationMail(String login);

	UserDTO refreshPassword(LoginDTO dto);

	Optional<User> findUserById(String id);

	List<User> findAllByRoles(UserTypeEnum userType);

	LoginResultDTO getDtoByUser(User user);

	User findByEmail(String email);

	void setUserRooms(List<Room> newUserRooms, User user);

	void setUserNotifications(List<Notification> newUserNotifications, User user);
}