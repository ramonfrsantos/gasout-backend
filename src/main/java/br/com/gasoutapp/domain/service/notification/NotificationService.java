package br.com.gasoutapp.domain.service.notification;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import br.com.gasoutapp.application.dto.notification.NotificationDTO;
import br.com.gasoutapp.application.dto.notification.PushResponseDTO;
import br.com.gasoutapp.application.dto.room.SensorGasPayloadDTO;
import br.com.gasoutapp.infrastructure.db.entity.notification.Notification;
import br.com.gasoutapp.infrastructure.db.entity.user.User;

public interface NotificationService {

	List<NotificationDTO> parseToDTO(List<Notification> list);

	NotificationDTO parseToDTO(Notification notification);

	List<NotificationDTO> getAllNotifications();

	List<NotificationDTO> getAllRecentNotifications(String login);

	NotificationDTO createNotification(NotificationDTO dto);

	String deleteNotification(String id);

	void setAllUserNotificationsNull(List<Notification> notifications, User user);

	Optional<Notification> findNotificationById(String id);

	PushResponseDTO sendPush(SensorGasPayloadDTO payload) throws IOException, URISyntaxException;
}