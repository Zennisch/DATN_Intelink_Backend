package intelink.exceptions;

import intelink.dto.response.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 400 - Bad Request

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ExceptionResponse response = ExceptionResponse.builder()
                .statusCode(status.value())
                .errorType(status.getReasonPhrase())
                .message("Validation failed: " + e.getBindingResult().getFieldError().getDefaultMessage())
                .build();
        return ResponseEntity.status(status).body(response);
    }

    // 401 - Unauthorized

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException e) {
        log.error("Authentication error: {}", e.getMessage());
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ExceptionResponse response = ExceptionResponse.builder()
                .statusCode(status.value())
                .errorType(status.getReasonPhrase())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException e) {
        log.error("Bad credentials: {}", e.getMessage());
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ExceptionResponse response = ExceptionResponse.builder()
                .statusCode(status.value())
                .errorType(status.getReasonPhrase())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(status).body(response);
    }

    // 403 - Forbidden

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied: {}", e.getMessage());
        HttpStatus status = HttpStatus.FORBIDDEN;
        ExceptionResponse response = ExceptionResponse.builder()
                .statusCode(status.value())
                .errorType(status.getReasonPhrase())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(status).body(response);
    }

    // 404 - Not Found
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(NoResourceFoundException e) {
        log.error("Resource not found: {}", e.getMessage());
        HttpStatus status = HttpStatus.NOT_FOUND;
        ExceptionResponse response = ExceptionResponse.builder()
                .statusCode(status.value())
                .errorType(status.getReasonPhrase())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(status).body(response);
    }

    // 500 - Internal Server Error

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        log.error("An error occurred: {}", e.getMessage(), e);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionResponse response = ExceptionResponse.builder()
                .statusCode(status.value())
                .errorType(status.getReasonPhrase())
                .message("An unexpected error occurred: " + e.getMessage())
                .build();
        return ResponseEntity.status(status).body(response);
    }

}
