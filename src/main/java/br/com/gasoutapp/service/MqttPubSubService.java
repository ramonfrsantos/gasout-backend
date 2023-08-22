package br.com.gasoutapp.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.iot.client.AWSIotException;

import br.com.gasoutapp.config.MQTTConfig;
import br.com.gasoutapp.domain.User;
import br.com.gasoutapp.dto.FirebaseNotificationDTO;
import br.com.gasoutapp.dto.NotificationDTO;
import br.com.gasoutapp.dto.PublishResponseDTO;
import br.com.gasoutapp.dto.RoomDTO;
import br.com.gasoutapp.dto.SensorDetailsDTO;
import br.com.gasoutapp.dto.SensorGasPayloadDTO;
import br.com.gasoutapp.security.CriptexCustom;

@Service
public class MqttPubSubService {

	@Autowired
	private MQTTConfig mqttConfig;

	@Autowired
	private UserService userService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private RoomService roomService;

	@Autowired
	private FirebaseService firebaseService;

	public PublishResponseDTO publishMessage(SensorGasPayloadDTO payload)
			throws AWSIotException, IOException, URISyntaxException {
		mqttConfig.connectToIot();
//		mqttConfig.publish(payload);

		PublishResponseDTO responseDTO = new PublishResponseDTO();
		responseDTO.setMessagePublished(true);

		String title = "";
		String body = "";

		Boolean notificationOn = false;
		Boolean alarmOn = false;
		Boolean sprinklersOn = false;

		Long sensorValue = payload.getMessage().getSensorValue();

		if (sensorValue <= 0) {
			title = "Apenas atualização de status...";
			body = "Tudo em paz! Sem vazamento de gás no momento.";

			notificationOn = true;
		} else if (sensorValue > 0 && sensorValue < 25) {
			title = "🚨 Atenção!";
			body = "Detectamos nível BAIXO de vazamento em seu local!";

			notificationOn = true;
		} else if (sensorValue >= 25 && sensorValue < 51) {
			title = "🚨🚨 Detectamos nível MÉDIO de vazamento em seu local! ";
			body = "Verifique as condições de monitoramento do seu cômodo...";

			notificationOn = true;
			alarmOn = true;
		} else if (sensorValue >= 51) {
			title = "🚨🚨🚨 Detectamos nível ALTO de vazamento em seu local!";
			body = "Entre agora em opções de monitoramento do seu cômodo para verificar o acionamento dos SPRINKLERS ou acione o SUPORTE TÉCNICO.";

			notificationOn = true;
			alarmOn = true;
			sprinklersOn = true;
		}

		String email = payload.getMessage().getEmail();

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

		notificationService.createNotification(notificationDTO);
		responseDTO.setNotificationCreated(true);

		SensorDetailsDTO details = new SensorDetailsDTO();
		details.setSensorValue(sensorValue);
		details.setName(payload.getMessage().getRoomName());
		details.setAlarmOn(alarmOn);
		details.setNotificationOn(notificationOn);
		details.setSprinklersOn(sprinklersOn);

		RoomDTO room = roomService.sendRoomSensorValue(details, email);
		responseDTO.setUpdatedRoom(room);

		return responseDTO;
	}

}
