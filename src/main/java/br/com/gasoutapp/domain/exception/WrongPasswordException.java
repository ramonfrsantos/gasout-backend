package br.com.gasoutapp.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class WrongPasswordException extends RuntimeException {

	@Serial
    private static final long serialVersionUID = 1L;

    public WrongPasswordException(String message) {
        super(message);
    }
}