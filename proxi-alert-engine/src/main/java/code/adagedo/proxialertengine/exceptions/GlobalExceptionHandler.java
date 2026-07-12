package code.adagedo.proxialertengine.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadySubscribedException.class)
    public ResponseEntity<ErrorResponse> handleException(UserAlreadySubscribedException exception){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setMessage(exception.getMessage());
        errorResponse.setStatusCode(HttpStatus.FORBIDDEN.value());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserNotSubscribedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotSubscribedException(UserNotSubscribedException exception){
        ErrorResponse errorResponse = new ErrorResponse();

        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setMessage(exception.getMessage());
        errorResponse.setStatusCode(HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(ChannelNameException.class)
    public ResponseEntity<ErrorResponse> handleChannelNameException(ChannelNameException exception){
        ErrorResponse errorResponse = new ErrorResponse();

        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setMessage(exception.getMessage());
        errorResponse.setStatusCode(HttpStatus.FORBIDDEN.value());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
}
