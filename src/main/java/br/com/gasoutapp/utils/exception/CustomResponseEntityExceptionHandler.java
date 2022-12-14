package br.com.gasoutapp.utils.exception;

import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.gasoutapp.exception.LoginNotFoundException;
import br.com.gasoutapp.exception.NotificationNotFoundException;
import br.com.gasoutapp.exception.RoomAlreadyExistsException;
import br.com.gasoutapp.exception.RoomNotFoundException;
import br.com.gasoutapp.exception.UserAlreadyRegisteredException;
import br.com.gasoutapp.exception.UserNotFoundException;
import br.com.gasoutapp.exception.WrongPasswordException;

@ControllerAdvice
@RestController
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  // personalizar responses para as exceptions

  @ExceptionHandler(Exception.class)
  public final ResponseEntity<Object> handleAllExceptions(Exception e, WebRequest request) {
    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), e.getMessage(),
        request.getDescription(false));

    return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(LoginNotFoundException.class)
  public final ResponseEntity<Object> handleLoginNotFoundException(LoginNotFoundException e,
      WebRequest request) {

    String detailsMessage = e
        .getMessage();

    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Falha no login.",
        detailsMessage);

    return new ResponseEntity<>(exceptionResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(WrongPasswordException.class)
  public final ResponseEntity<Object> handleWrongPasswordException(WrongPasswordException e,
      WebRequest request) {

    String detailsMessage = e
        .getMessage();

    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Falha no login.",
        detailsMessage);

    return new ResponseEntity<>(exceptionResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(NotificationNotFoundException.class)
  public final ResponseEntity<Object> handleNotificationNotFoundException(NotificationNotFoundException e,
      WebRequest request) {

    String detailsMessage = e
        .getMessage();

    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Erro.",
        detailsMessage);

    return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(RoomNotFoundException.class)
  public final ResponseEntity<Object> handleRoomNotFoundException(RoomNotFoundException e,
      WebRequest request) {

    String detailsMessage = e
        .getMessage();

    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Erro.",
        detailsMessage);

    return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public final ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException e,
      WebRequest request) {

    String detailsMessage = e
        .getMessage();

    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Erro.",
        detailsMessage);

    return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserAlreadyRegisteredException.class)
  public final ResponseEntity<Object> handleUserAlreadyRegisteredException(UserAlreadyRegisteredException e,
      WebRequest request) {

    String detailsMessage = e
        .getMessage();

    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Erro.",
        detailsMessage);

    return new ResponseEntity<>(exceptionResponse, HttpStatus.FOUND);
  }

  @ExceptionHandler(RoomAlreadyExistsException.class)
  public final ResponseEntity<Object> handleRoomAlreadyExistsException(RoomAlreadyExistsException e,
      WebRequest request) {

    String detailsMessage = e
        .getMessage();

    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "Erro.",
        detailsMessage);

    return new ResponseEntity<>(exceptionResponse, HttpStatus.FOUND);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException e,
      HttpHeaders headers, HttpStatus status, WebRequest request) {

    String detailsMessage = e
        .getBindingResult()
        .getAllErrors()
        .toString()
        .replace("]", "")
        .replace(" [","")
        .split("default message")[2];

    ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(), "A valida????o falhou.",
        detailsMessage);

    return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
  }

}