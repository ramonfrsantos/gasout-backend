package br.com.gasoutapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FOUND)
public class RoomAlreadyExistsException extends RuntimeException {
    
	public RoomAlreadyExistsException() {
        super("Esse cômodo já foi cadastrado.");
    }

    public RoomAlreadyExistsException(String message) {
        super(message);
    }
}