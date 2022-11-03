package br.com.gasoutapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class LoginNotFoundException extends RuntimeException {
	public LoginNotFoundException() {
        super("Dados de login incorretos.");
    }

    public LoginNotFoundException(String message) {
        super(message);
    }


}
