package br.com.gasoutapp.application.dto.room;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SensorGasPayloadDTO {
	private List<SensorDTO> sensors;
}
