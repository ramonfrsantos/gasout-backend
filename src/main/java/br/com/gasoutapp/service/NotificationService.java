package br.com.gasoutapp.service;

import static br.com.gasoutapp.utils.JsonUtil.convertToObjectArray;
import static br.com.gasoutapp.utils.StringUtils.reverseList;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.gasoutapp.config.security.CriptexCustom;
import br.com.gasoutapp.domain.Notification;
import br.com.gasoutapp.domain.User;
import br.com.gasoutapp.dto.FirebaseNotificationDTO;
import br.com.gasoutapp.dto.NotificationDTO;
import br.com.gasoutapp.dto.PushResponseDTO;
import br.com.gasoutapp.dto.RevisionDTO;
import br.com.gasoutapp.dto.RoomDTO;
import br.com.gasoutapp.dto.SensorDetailsDTO;
import br.com.gasoutapp.dto.SensorGasPayloadDTO;
import br.com.gasoutapp.exception.NotFoundException;
import br.com.gasoutapp.repository.NotificationRepository;

@Service
public class NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private RoomService roomService;

	@Autowired
	private AuditReader auditReader;

	@Autowired
	private FirebaseService firebaseService;

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

	public PushResponseDTO sendPush(SensorGasPayloadDTO payload) throws IOException, URISyntaxException {
		PushResponseDTO responseDTO = new PushResponseDTO();

		String title = "";
		String body = "";

		Long sensorValue = payload.getDetails().getSensorValue();

		if (sensorValue <= 0) {
			title = "Apenas atualização de status...";
			body = "Tudo em paz! Sem vazamento de gás no momento.";
		} else if (sensorValue > 0 && sensorValue < 25) {
			title = "🚨 Atenção!";
			body = "Detectamos nível BAIXO de vazamento em seu local!";
		} else if (sensorValue >= 25 && sensorValue < 51) {
			title = "🚨🚨 Detectamos nível MÉDIO de vazamento em seu local! ";
			body = "Verifique as condições de monitoramento do seu cômodo...";
		} else if (sensorValue >= 51) {
			title = "🚨🚨🚨 Detectamos nível ALTO de vazamento em seu local!";
			body = "Entre agora em opções de monitoramento do seu cômodo para verificar o acionamento dos SPRINKLERS ou acione o SUPORTE TÉCNICO.";
		}

		String email = payload.getDetails().getUserEmail();

		NotificationDTO notificationDTO = new NotificationDTO();
		notificationDTO.setEmail(email);
		notificationDTO.setMessage(body);
		notificationDTO.setTitle(title);

		User user = userService.findByLogin(email);

		List<String> ids = new ArrayList<>();
		ids.add(CriptexCustom.decrypt(user.getTokenFirebase()));

		FirebaseNotificationDTO firebaseNotificationDTO = new FirebaseNotificationDTO();
		firebaseNotificationDTO.setNotification(notificationDTO);
		firebaseNotificationDTO.setRegistration_ids(ids);

		firebaseService.createFirebaseNotification(firebaseNotificationDTO);
		responseDTO.setPushNotificationSent(true);

		createNotification(notificationDTO);
		responseDTO.setNotificationCreated(true);

		SensorDetailsDTO details = new SensorDetailsDTO();
		details.setSensorValue(sensorValue);
		details.setRoomName(payload.getDetails().getRoomName());
		details.setUserEmail(email);

		RoomDTO room = roomService.sendRoomSensorValue(details);
		responseDTO.setUpdatedRoom(room);

		return responseDTO;
	}

	public List<RevisionDTO> getRevisions(String id) {
		AuditQuery auditQuery = auditReader.createQuery().forRevisionsOfEntityWithChanges(Notification.class, true)
				.add(AuditEntity.id().eq(id));

		List<RevisionDTO> details = new ArrayList<RevisionDTO>();

		for (Object revision : auditQuery.getResultList()) {
			RevisionDTO r = new RevisionDTO();

			Object[] objArray = convertToObjectArray(revision);

			r.setEntity(objArray[0]);
			r.setRevisionDetails(objArray[1]);
			r.setRevisionType(objArray[2]);
			r.setUpdatedAttributes(objArray[3]);
			details.add(r);
		}

		return details;
	}
}