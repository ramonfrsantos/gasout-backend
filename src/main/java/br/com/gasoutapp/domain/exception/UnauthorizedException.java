package br.com.gasoutapp.domain.exception;

import org.hibernate.service.spi.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "UnauthorizedException")
public class UnauthorizedException extends ServiceException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2442683352052122618L;

	public UnauthorizedException() {
        super("Não autorizado.");
    }
}
