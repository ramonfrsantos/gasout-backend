package br.com.gasoutapp.controller;

import br.com.gasoutapp.dto.BaseResponseDTO;

public abstract class BaseRestController {

	protected BaseResponseDTO buildResponse(Object object) {
		BaseResponseDTO response = new BaseResponseDTO();
		response.setData(object);
		return response;
	}

	protected BaseResponseDTO buildResponseMessage(String message) {
		BaseResponseDTO response = new BaseResponseDTO();
		response.setMessage(message);
		return response;
	}
}
