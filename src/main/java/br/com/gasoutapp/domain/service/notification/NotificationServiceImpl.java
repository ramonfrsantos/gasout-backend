package br.com.gasoutapp.domain.service.notification;

import static br.com.gasoutapp.infrastructure.utils.JsonUtil.convertToObjectArray;
import static br.com.gasoutapp.infrastructure.utils.StringUtils.reverseList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.application.dto.audit.RevisionDTO;
import br.com.gasoutapp.application.dto.notification.FirebaseNotificationDTO;
import br.com.gasoutapp.application.dto.notification.NotificationDTO;
import br.com.gasoutapp.application.dto.notification.PushResponseDTO;
import br.com.gasoutapp.application.dto.room.SensorDTO;
import br.com.gasoutapp.application.dto.room.SensorGasPayloadDTO;
import br.com.gasoutapp.application.web.firebase.FirebaseService;
import br.com.gasoutapp.domain.exception.NotFoundException;
import br.com.gasoutapp.domain.service.room.RoomService;
import br.com.gasoutapp.domain.service.user.UserService;
import br.com.gasoutapp.infrastructure.config.security.EncryptorCustom;
import br.com.gasoutapp.infrastructure.db.entity.enums.SensorTypeEnum;
import br.com.gasoutapp.infrastructure.db.entity.notification.Notification;
import br.com.gasoutapp.infrastructure.db.entity.user.User;
import br.com.gasoutapp.infrastructure.db.repository.NotificationRepository;

import javax.persistence.EntityManagerFactory;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private RoomService roomService;

	@Autowired
	private EntityManagerFactory factory;

	@Autowired
	private FirebaseService firebaseService;

    public List<NotificationDTO> getAllNotifications() {
		return parseToDTO(notificationRepository.findAll());
	}

	public List<NotificationDTO> getAllRecentNotifications(String login) {
		var user = userService.findByEmail(login);
		var notifications = notificationRepository.findAllByUserEmailOrderByDateAsc(user.getEmail());

		reverseList(notifications);

		return parseToDTO(notifications);
	}

	public NotificationDTO createNotification(NotificationDTO dto) {
		List<Notification> newUserNotifications = new ArrayList<>();

		User newUser;

		var user = userService.findByEmail(dto.getUserEmail());

		if (Objects.isNull(user)) {
			throw new NotFoundException("Usuario nao encontrado.");
		}

		newUser = user;

		var notifications = notificationRepository.findAllByUserEmailOrderByDateAsc(user.getEmail());
		if (notifications.size() >= 10) {
			setAllUserNotificationsNull(notifications, user);
		} else {
			newUserNotifications = notifications;
		}

		var newNotification = new Notification();

		newNotification.setUserEmail(user.getEmail());
		newNotification.setTitle(dto.getTitle());
		newNotification.setMessage(dto.getMessage());
		newNotification.setDate(new Date());

		newNotification = notificationRepository.save(newNotification);
		newUserNotifications.add(newNotification);

		userService.setUserNotifications(newUserNotifications, newUser);

		var notificationsUserNull = notificationRepository.findAllByUserEmailOrderByDateAsc(null);

		if (!notificationsUserNull.isEmpty()) {
			for (Notification notification : notificationsUserNull) {
				notification.setDeleted(true);
				notificationRepository.save(notification);
			}
		}

		return parseToDTO(newNotification);
	}

	public String deleteNotification(String id) {
		var notification = notificationRepository.findById(id)
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
		var responseDTO = new PushResponseDTO();

		for (SensorDTO sensor : payload.getSensors()) {
			var details = new SensorDTO();
			
			var roomName = roomService.getRoomNameById(sensor.getRoomNameId());
			var email = sensor.getUserEmail();
			
			var user = userService.findByEmail(email);

			roomService.findAllByUserEmail(email).forEach(room -> {
				if (room.getName() == roomName) {
                    var sensorValue = sensor.getSensorValue();
					
					details.setSensorValue(sensorValue);
					details.setSensorType(sensor.getSensorType());
					details.setRoomNameId(roomName.getNameId());
					details.setUserEmail(email);
					
					if(sensor.getSensorType() == SensorTypeEnum.GAS && room.isNotificationOn()) {
						List<String> ids = new ArrayList<>();
						ids.add(EncryptorCustom.decrypt(user.getTokenFirebase()));
						
						var notificationDTO = createNotificationContentBasedOnGasValue(sensorValue, email);
						
						var firebaseNotificationDTO = new FirebaseNotificationDTO();
						firebaseNotificationDTO.setNotification(notificationDTO);
						firebaseNotificationDTO.setRegistrationIds(ids);

						firebaseService.createFirebaseNotification(firebaseNotificationDTO);
	
						responseDTO.setPushNotificationSent(true);

						createNotification(notificationDTO);
						responseDTO.setNotificationCreated(true);
					} else {
						responseDTO.setPushNotificationSent(false);
						responseDTO.setNotificationCreated(false);
					}

				}
			});
			
			var roomDTO = roomService.sendRoomSensorValue(details);
			responseDTO.setUpdatedRoom(roomDTO);
		}

		return responseDTO;
	}
	
	private NotificationDTO createNotificationContentBasedOnGasValue(Long gasSensorValue, String email) {
		String title;
		String body;
		
		if (gasSensorValue <= 0) {
			title = "Apenas atualização de status...";
			body = "Tudo em paz! Sem vazamento de gás no momento.";
		} else if (gasSensorValue <= 25) {
			title = "🚨 Atenção!";
			body = "Detectamos nível BAIXO de vazamento em seu local!";
		} else if (gasSensorValue <= 50) {
			title = "🚨🚨 Detectamos nível MÉDIO de vazamento em seu local! ";
			body = "Verifique as condições de monitoramento do seu cômodo...";
		} else {
			title = "🚨🚨🚨 Detectamos nível ALTO de vazamento em seu local!";
			body = "Entre agora em opções de monitoramento do seu cômodo para verificar o acionamento dos SPRINKLERS ou acione o SUPORTE TÉCNICO.";
		}
		
		var notificationDTO = new NotificationDTO();
		notificationDTO.setUserEmail(email);
		notificationDTO.setMessage(body);
		notificationDTO.setTitle(title);
		
		return notificationDTO;
	}
	public List<NotificationDTO> parseToDTO(List<Notification> list) {
		return list.stream().map(this::parseToDTO).toList();
	}

	public NotificationDTO parseToDTO(Notification notification) {
		return new NotificationDTO(notification);
	}

	@Override
	public List<RevisionDTO> getRevisions(String id) {
		var auditQuery = getAuditQuery(id);

		List<RevisionDTO> details = new ArrayList<>();

		for (Object revision : auditQuery.getResultList()) {
			var r = new RevisionDTO();

			var objArray = convertToObjectArray(revision);
			r.setEntity(objArray[0]);
			r.setRevisionDetails(objArray[1]);
			r.setRevisionType(objArray[2]);
			r.setUpdatedAttributes(objArray[3]);

			details.add(r);
		}

		return details;
	}

	private AuditQuery getAuditQuery(String id) {
		var auditReader = AuditReaderFactory.get(factory.createEntityManager());

		return auditReader.createQuery().forRevisionsOfEntityWithChanges(Notification.class, true)
				.add(AuditEntity.id().eq(id));
	}
}