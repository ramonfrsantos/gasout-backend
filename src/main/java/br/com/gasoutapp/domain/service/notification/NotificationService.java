package br.com.gasoutapp.domain.service.notification;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import br.com.gasoutapp.application.dto.audit.RevisionDTO;
import br.com.gasoutapp.application.dto.notification.NotificationDTO;
import br.com.gasoutapp.application.dto.notification.PushResponseDTO;
import br.com.gasoutapp.application.dto.room.SensorGasPayloadDTO;
import br.com.gasoutapp.infrastructure.db.entity.notification.Notification;
import br.com.gasoutapp.infrastructure.db.entity.user.User;

public interface NotificationService {

	public List<NotificationDTO> parseToDTO(List<Notification> list);

	public Page<NotificationDTO> parseToDTO(Page<Notification> page);

	public NotificationDTO parseToDTO(Notification notification);

	public List<NotificationDTO> getAllNotifications();

	public List<NotificationDTO> getAllRecentNotifications(String login);

	public NotificationDTO createNotification(NotificationDTO dto);

	public String deleteNotification(String id);

	public void setAllUserNotificationsNull(List<Notification> notifications, User user);

	public Optional<Notification> findNotificationById(String id);

	public PushResponseDTO sendPush(SensorGasPayloadDTO payload) throws IOException, URISyntaxException;

	public List<RevisionDTO> getRevisions(String id);
}