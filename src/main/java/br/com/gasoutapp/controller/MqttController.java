package br.com.gasoutapp.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.iot.client.AWSIotException;

import br.com.gasoutapp.dto.BaseResponseDTO;
import br.com.gasoutapp.dto.SensorGasPayloadDTO;
import br.com.gasoutapp.service.MqttPubSubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("mqtt-broker")
public class MqttController extends BaseRestController {
	@Autowired
	private MqttPubSubService service;

	@PostMapping("/publish")
	@Operation(summary = "Publicar em um tópico", security = @SecurityRequirement(name = "gasoutapp"))
	public BaseResponseDTO publishMessage(@RequestBody SensorGasPayloadDTO payload) throws AWSIotException, IOException, URISyntaxException {
		return buildResponse(service.publishMessage(payload));
	}
}