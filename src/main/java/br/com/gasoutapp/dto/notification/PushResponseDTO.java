package br.com.gasoutapp.dto.notification;

import br.com.gasoutapp.dto.room.RoomDTO;
import lombok.Data;

@Data
public class PushResponseDTO {

	private Boolean pushNotificationSent;
	private Boolean notificationCreated;
	private RoomDTO updatedRoom;

}
