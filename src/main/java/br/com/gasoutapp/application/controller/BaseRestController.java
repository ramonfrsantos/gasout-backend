package br.com.gasoutapp.application.controller;

import br.com.gasoutapp.application.dto.BaseResponseDTO;

public abstract class BaseRestController {

	protected BaseResponseDTO buildResponse(Object object) {
		BaseResponseDTO response = new BaseResponseDTO();
		response.setData(object);
		return response;
	}

}