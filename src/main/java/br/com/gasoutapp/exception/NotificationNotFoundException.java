package br.com.gasoutapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class NotificationNotFoundException extends RuntimeException {
	public NotificationNotFoundException() {
        super("Notificação não encontrada.");
    }

    public NotificationNotFoundException(String message) {
        super(message);
    }
}