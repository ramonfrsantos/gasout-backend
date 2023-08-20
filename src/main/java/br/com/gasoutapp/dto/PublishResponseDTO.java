package br.com.gasoutapp.dto;

import lombok.Data;

@Data
public class PublishResponseDTO {

	private Boolean messagePublished;
	private Boolean pushNotificationSent;
	private Boolean notificationCreated;
	private RoomDTO updatedRoom;

}
