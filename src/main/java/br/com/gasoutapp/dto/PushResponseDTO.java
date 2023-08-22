package br.com.gasoutapp.dto;

import lombok.Data;

@Data
public class PushResponseDTO {

	private Boolean pushNotificationSent;
	private Boolean notificationCreated;
	private RoomDTO updatedRoom;

}
