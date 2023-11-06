package br.com.gasoutapp.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResponseDTO {

	private Object data;
	private String message;
	
	public BaseResponseDTO(String dataHora, String message) {
		this.data = dataHora;
		this.message = message;
	}
}
