package br.com.gasoutapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

	public UserNotFoundException() {
        super("Usuário não encontrado.");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}