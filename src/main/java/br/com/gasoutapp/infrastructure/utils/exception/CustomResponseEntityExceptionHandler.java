package br.com.gasoutapp.infrastructure.utils.exception;

import java.util.Date;

import br.com.gasoutapp.domain.exception.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@RestController
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(Exception.class)
	public final ResponseEntity<Object> handleAllExceptions(Exception e, WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), e.getMessage(),
				request.getDescription(false));

		return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(WrongPasswordException.class)
	public final ResponseEntity<Object> handleWrongPasswordException(WrongPasswordException e, WebRequest request) {

		String detailsMessage = e.getMessage();

		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Falha no login.", detailsMessage);

		return new ResponseEntity<>(exceptionResponse, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(NotFoundException.class)
	public final ResponseEntity<Object> handleUserNotFoundException(NotFoundException e, WebRequest request) {

		String detailsMessage = e.getMessage();

		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Erro. Usuário não encontrado.", detailsMessage);

		return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(UserAlreadyRegisteredException.class)
	public final ResponseEntity<Object> handleUserAlreadyRegisteredException(UserAlreadyRegisteredException e,
			WebRequest request) {

		String detailsMessage = e.getMessage();

		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Erro. Usuário já registrado.", detailsMessage);

		return new ResponseEntity<>(exceptionResponse, HttpStatus.FOUND);
	}

	@ExceptionHandler(AlreadyExistsException.class)
	public final ResponseEntity<Object> handleRoomAlreadyExistsException(AlreadyExistsException e,
			WebRequest request) {

		String detailsMessage = e.getMessage();

		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Erro. Cômodo já registrado.", detailsMessage);

		return new ResponseEntity<>(exceptionResponse, HttpStatus.FOUND);
	}
	
	@ExceptionHandler(UnauthorizedException.class)
	public final ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException e, WebRequest request) {

		String detailsMessage = e.getMessage();

		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Erro. Não autorizado.", detailsMessage);

		return new ResponseEntity<>(exceptionResponse, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(EncryptionException.class)
	public final ResponseEntity<Object> handleEncryptionException(EncryptionException e, WebRequest request) {

		String detailsMessage = e.getMessage();

		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Erro. Não foi possível criptografar a senha. Verifique o método de criptografia.", detailsMessage);

		return new ResponseEntity<>(exceptionResponse, HttpStatus.UNAUTHORIZED);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		String detailsMessage = e.getBindingResult().getAllErrors().toString().replace("]", "").replace(" [", "")
				.split("default message")[2];

		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "A validação falhou.", detailsMessage);

		return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
	}

}