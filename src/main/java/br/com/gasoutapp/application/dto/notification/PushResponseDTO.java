package br.com.gasoutapp.application.dto.notification;

import br.com.gasoutapp.application.dto.room.RoomDTO;
import lombok.Data;

@Data
public class PushResponseDTO {

	private Boolean pushNotificationSent;
	private Boolean notificationCreated;
	private RoomDTO updatedRoom;

}
