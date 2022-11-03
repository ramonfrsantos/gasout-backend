package br.com.gasoutapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class WrongPasswordException extends RuntimeException {

	public WrongPasswordException() {
        super("Senha incorreta.");
    }

    public WrongPasswordException(String message) {
        super(message);
    }
}