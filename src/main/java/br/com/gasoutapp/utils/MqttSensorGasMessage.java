package br.com.gasoutapp.utils;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

public class MqttSensorGasMessage extends AWSIotMessage {
    public MqttSensorGasMessage(String topic, AWSIotQos qos, String payload) {
    	super(topic, qos, payload);
    }
    
    @Override
    public void onSuccess() {
    	System.out.println("Mensagem publicada com sucesso.");
    }
    
    @Override
    public void onFailure() {
    	System.out.println("Falha na publicação da mensagem.");    	
    }
    
    @Override
    public void onTimeout() {
    	System.out.println("Timeout.");    	
    }
}
