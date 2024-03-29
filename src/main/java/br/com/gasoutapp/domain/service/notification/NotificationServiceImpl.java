package br.com.gasoutapp.domain.service.notification;

import static br.com.gasoutapp.infrastructure.utils.JsonUtil.convertToObjectArray;
import static br.com.gasoutapp.infrastructure.utils.StringUtils.reverseList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.application.dto.audit.RevisionDTO;
import br.com.gasoutapp.application.dto.notification.FirebaseNotificationDTO;
import br.com.gasoutapp.application.dto.notification.NotificationDTO;
import br.com.gasoutapp.application.dto.notification.PushResponseDTO;
import br.com.gasoutapp.application.dto.room.RoomDTO;
import br.com.gasoutapp.application.dto.room.SensorDTO;
import br.com.gasoutapp.application.dto.room.SensorGasPayloadDTO;
import br.com.gasoutapp.application.web.firebase.FirebaseService;
import br.com.gasoutapp.domain.exception.NotFoundException;
import br.com.gasoutapp.domain.service.room.RoomService;
import br.com.gasoutapp.domain.service.user.UserService;
import br.com.gasoutapp.infrastructure.config.security.CriptexCustom;
import br.com.gasoutapp.infrastructure.db.entity.enums.RoomNameEnum;
import br.com.gasoutapp.infrastructure.db.entity.enums.SensorTypeEnum;
import br.com.gasoutapp.infrastructure.db.entity.notification.Notification;
import br.com.gasoutapp.infrastructure.db.entity.room.Room;
import br.com.gasoutapp.infrastructure.db.entity.user.User;
import br.com.gasoutapp.infrastructure.db.repository.NotificationRepository;

@Service
public class NotificationServiceImpl implements NotificationService {

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

	public List<NotificationDTO> getAllNotifications() {
		return parseToDTO(notificationRepository.findAll());
	}

	public List<NotificationDTO> getAllRecentNotifications(String login) {
		User user = userService.findByLogin(login);
		List<Notification> notifications = notificationRepository.findAllByUserEmailOrderByDateAsc(user.getEmail());
		reverseList(notifications);

		return parseToDTO(notifications);
	}

	public NotificationDTO createNotification(NotificationDTO dto) {
		List<Notification> newUserNotifications = new ArrayList<>();

		User newUser;
		User user = userService.findByLogin(dto.getUserEmail());

		if (Objects.isNull(user)) {
			throw new NotFoundException("Usuario nao encontrado.");
		}
		newUser = user;

		List<Notification> notifications = notificationRepository.findAllByUserEmailOrderByDateAsc(user.getEmail());
		if (notifications.size() >= 10) {
			setAllUserNotificationsNull(notifications, user);
		} else {
			newUserNotifications = notifications;
		}

		Notification newNotification = new Notification();

		newNotification.setUserEmail(user.getEmail());
		newNotification.setTitle(dto.getTitle());
		newNotification.setMessage(dto.getMessage());
		newNotification.setDate(new Date());

		newNotification = notificationRepository.save(newNotification);
		newUserNotifications.add(newNotification);

		userService.setUserNotifications(newUserNotifications, newUser);

		List<Notification> notificationsUserNull = notificationRepository.findAllByUserEmailOrderByDateAsc(null);

		if (!notificationsUserNull.isEmpty()) {
			for (Notification notification : notificationsUserNull) {
				notification.setDeleted(true);
				notificationRepository.save(notification);
			}
		}

		return parseToDTO(newNotification);
	}

	public String deleteNotification(String id) {
		Notification notification = notificationRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Notificação não encontrada."));

		notification.setDeleted(true);
		notificationRepository.save(notification);

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
			notification.setUserEmail(null);
			notificationRepository.save(notification);
		}
	}

	public Optional<Notification> findNotificationById(String id) {
		return notificationRepository.findById(id);
	}

	public PushResponseDTO sendPush(SensorGasPayloadDTO payload) {
		PushResponseDTO responseDTO = new PushResponseDTO();
		

		for (SensorDTO sensor : payload.getSensors()) {
			SensorDTO details = new SensorDTO();
			
			RoomNameEnum roomName = roomService.getRoomNameById(sensor.getRoomNameId());
			String email = sensor.getUserEmail();
			
			User user = userService.findByEmail(email);

			roomService.findAllByUserEmail(email).forEach(room -> {
				if (room.getName() == roomName) {
					Room userRoom = room;
										
					Long sensorValue = sensor.getSensorValue();
					
					details.setSensorValue(sensorValue);
					details.setSensorType(sensor.getSensorType());
					details.setRoomNameId(roomName.getNameId());
					details.setUserEmail(email);
					
					if(sensor.getSensorType() == SensorTypeEnum.GAS && userRoom.isNotificationOn()) {
						List<String> ids = new ArrayList<>();
						ids.add(CriptexCustom.decrypt(user.getTokenFirebase()));
						
						NotificationDTO notificationDTO = createNotificationContentBasedOnGasValue(sensorValue, email);
						
						FirebaseNotificationDTO firebaseNotificationDTO = new FirebaseNotificationDTO();
						firebaseNotificationDTO.setNotification(notificationDTO);
						firebaseNotificationDTO.setRegistration_ids(ids);

						try {
							firebaseService.createFirebaseNotification(firebaseNotificationDTO);
						} catch (IOException | URISyntaxException e) {
							e.printStackTrace();
						} 
	
						responseDTO.setPushNotificationSent(true);

						createNotification(notificationDTO);
						responseDTO.setNotificationCreated(true);
					} else {
						responseDTO.setPushNotificationSent(false);
						responseDTO.setNotificationCreated(false);
					}

				}
			});
			
			RoomDTO roomDTO = roomService.sendRoomSensorValue(details);
			responseDTO.setUpdatedRoom(roomDTO);
		}
		

		return responseDTO;
	}
	
	private NotificationDTO createNotificationContentBasedOnGasValue(Long gasSensorValue, String email) {
		String title = "";
		String body = "";
		
		if (gasSensorValue <= 0) {
			title = "Apenas atualização de status...";
			body = "Tudo em paz! Sem vazamento de gás no momento.";
		} else if (gasSensorValue <= 24) {
			title = "🚨 Atenção!";
			body = "Detectamos nível BAIXO de vazamento em seu local!";
		} else if (gasSensorValue <= 50) {
			title = "🚨🚨 Detectamos nível MÉDIO de vazamento em seu local! ";
			body = "Verifique as condições de monitoramento do seu cômodo...";
		} else {
			title = "🚨🚨🚨 Detectamos nível ALTO de vazamento em seu local!";
			body = "Entre agora em opções de monitoramento do seu cômodo para verificar o acionamento dos SPRINKLERS ou acione o SUPORTE TÉCNICO.";
		}
		
		NotificationDTO notificationDTO = new NotificationDTO();
		notificationDTO.setUserEmail(email);
		notificationDTO.setMessage(body);
		notificationDTO.setTitle(title);
		
		return notificationDTO;
	}

	public List<RevisionDTO> getRevisions(String id) {
		AuditQuery auditQuery = auditReader.createQuery().forRevisionsOfEntityWithChanges(Notification.class, true)
				.add(AuditEntity.id().eq(id));

		List<RevisionDTO> details = new ArrayList<>();

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

	public List<NotificationDTO> parseToDTO(List<Notification> list) {
		return list.stream().map(this::parseToDTO).toList();
	}

	public Page<NotificationDTO> parseToDTO(Page<Notification> page) {
		return page.map(NotificationDTO::new);
	}

	public NotificationDTO parseToDTO(Notification notification) {
		return new NotificationDTO(notification);
	}
}