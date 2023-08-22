package br.com.gasoutapp.service;

import static br.com.gasoutapp.utils.StringUtils.reverseList;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.gasoutapp.domain.Notification;
import br.com.gasoutapp.domain.User;
import br.com.gasoutapp.dto.NotificationDTO;
import br.com.gasoutapp.exception.NotFoundException;
import br.com.gasoutapp.repository.NotificationRepository;

@Service
public class NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private UserService userService;

	public List<NotificationDTO> parseToDTO(List<Notification> list) {
		return list.stream().map(v -> parseToDTO(v)).collect(Collectors.toList());
	}

	public Page<NotificationDTO> parseToDTO(Page<Notification> page) {
		return page.map(NotificationDTO::new);
	}

	public NotificationDTO parseToDTO(Notification notification) {
		return new NotificationDTO(notification);
	}

	public List<NotificationDTO> getAllNotifications() {
		return parseToDTO(notificationRepository.findAll());
	}

	public List<NotificationDTO> getAllRecentNotifications(String login) {
		User user = userService.findByLogin(login);
		List<Notification> notifications = notificationRepository.findAllByUserOrderByDateAsc(user);
		reverseList(notifications);

		return parseToDTO(notifications);
	}

	public ResponseEntity<NotificationDTO> createNotification(NotificationDTO dto) {
		List<Notification> newUserNotifications = new ArrayList<>();
		User newUser;
		User user = userService.findByLogin(dto.getEmail());
		if (user == null) {
			throw new NotFoundException("Usuario nao encontrado.");
		}
		newUser = user;

		List<Notification> notifications = notificationRepository.findAllByUserOrderByDateAsc(user);
		if (notifications.size() >= 10) {
			setAllUserNotificationsNull(notifications, user);
		} else {
			newUserNotifications = notifications;
		}

		Notification newNotification = new Notification();

		newNotification.setUser(user);
		newNotification.setTitle(dto.getTitle());
		newNotification.setMessage(dto.getMessage());
		newNotification.setDate(new Date());

		newNotification = notificationRepository.save(newNotification);
		newUserNotifications.add(newNotification);

		userService.setUserNotifications(newUserNotifications, newUser);

		List<Notification> notificationsUserNull = notificationRepository.findAllByUserOrderByDateAsc(null);
		if (notificationsUserNull.size() > 0) {
			for (Notification notification : notificationsUserNull) {
				notification.setDeleted(true);
				notificationRepository.save(notification);
			}
		}

		URI locationNotification = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(newNotification.getId()).toUri();

		return ResponseEntity.created(locationNotification).body(parseToDTO(newNotification));
	}

	public String deleteNotification(String id) {
		Notification notification = notificationRepository.getById(id);
		if (notification == null) {
			throw new NotFoundException("Notificação não encontrada.");
		} else {
			notification.setDeleted(true);
			notificationRepository.save(notification);
		}

		return "Registro excluido com sucesso.";
	}

	public void setAllUserNotificationsNull(List<Notification> notifications, User user) {
		if (user != null) {
			User newUser;
			newUser = user;
			List<Notification> newNotificationsList = new ArrayList<>();
			userService.setUserNotifications(newNotificationsList, newUser);
		}
		for (Notification notification : notifications) {
			notification.setUser(null);
			notificationRepository.save(notification);
		}
	}

	public Optional<Notification> findNotificationById(String id) {
		return notificationRepository.findById(id);
	}
}