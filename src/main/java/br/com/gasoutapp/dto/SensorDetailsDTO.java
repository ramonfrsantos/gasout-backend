package br.com.gasoutapp.dto;

import lombok.Data;

@Data
public class SensorDetailsDTO {
	private String name;
  private boolean alarmOn;
	private boolean notificationOn;
	private boolean sprinklersOn;	
	private Integer sensorValue;
}