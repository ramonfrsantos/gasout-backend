package br.com.gasoutapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.iot.client.AWSIotException;
import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.gasoutapp.config.MQTTConfig;
import br.com.gasoutapp.dto.SensorGasPayloadDTO;

@Service
public class MqttPubSubService {

	@Autowired
	private MQTTConfig mqttConfig;

	public String publishMessage(SensorGasPayloadDTO payload) throws AWSIotException, JsonProcessingException {
		mqttConfig.connectToIot();
		mqttConfig.publish(payload);

		return "Mensagem publicada com sucesso.";
	}

}
