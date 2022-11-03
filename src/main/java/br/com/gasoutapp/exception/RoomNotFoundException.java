package br.com.gasoutapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class RoomNotFoundException extends RuntimeException {

	public RoomNotFoundException() {
        super("Cômodo com o id informado não está cadastrado.");
    }

    public RoomNotFoundException(String message) {
        super(message);
    }
}