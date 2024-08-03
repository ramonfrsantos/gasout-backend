package br.com.gasoutapp.domain.exception;

import org.hibernate.service.spi.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "UnauthorizedException")
public class UnauthorizedException extends ServiceException {

	@Serial
	private static final long serialVersionUID = 1L;

	public UnauthorizedException(String message) {
        super(message);
    }
}
