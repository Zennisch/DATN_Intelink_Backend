package intelink.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;
    private Instant timestamp;
    private Map<String, String> validationErrors;

    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponse withValidationErrors(int status, String error,
                                                   String message, String path,
                                                   Map<String, String> validationErrors) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(Instant.now())
                .validationErrors(validationErrors)
                .build();
    }
}