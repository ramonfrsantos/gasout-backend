package br.com.gasoutapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FOUND)
public class UserAlreadyRegisteredException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserAlreadyRegisteredException() {
        super("Usuário com esse email já foi cadastrado.");
    }

    public UserAlreadyRegisteredException(String message) {
        super(message);
    }
}