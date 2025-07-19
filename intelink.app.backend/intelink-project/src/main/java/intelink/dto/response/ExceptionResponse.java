package intelink.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ExceptionResponse {

    private int statusCode;
    private String errorType;
    private String message;

    @Builder.Default
    private String details = "No additional details provided.";

    @Builder.Default
    private Instant timestamp = Instant.now();

}
