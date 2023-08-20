package br.com.gasoutapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gasoutapp.dto.SensorGasPayloadDTO;
import br.com.gasoutapp.utils.MqttSensorGasMessage;

@Configuration
public class MQTTConfig {

	@Value("${mqtt.client.url}")
	String clientURL;

	@Value("${mqtt.client.id}")
	String clientId;

	@Value("${aws.client.access-key.id}")
	String awsAccessKeyId;

	@Value("${aws.client.access-key.secret}")
	String awsSecretAccessKey;

	AWSIotMqttClient client = null;

	@SuppressWarnings("deprecation")
	public void connectToIot() throws AWSIotException {
		client = new AWSIotMqttClient(clientURL, clientId, awsAccessKeyId, awsSecretAccessKey);
		client.connect();
		System.out.println("Conectado ao Iot.");
	}

	public void publish(SensorGasPayloadDTO payload) throws AWSIotException, JsonProcessingException {
		String topic = "gas_out_topic";
		AWSIotQos qos = AWSIotQos.QOS0;
		long timeout = 3000;
		ObjectMapper mapper = new ObjectMapper();

		MqttSensorGasMessage message = new MqttSensorGasMessage(topic, qos, mapper.writeValueAsString(payload));

		client.publish(message, timeout);
	}
}
