package intelink.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionResponse {

    private int statusCode;
    private String errorType;
    private String message;
    private String details;

    @Builder.Default
    private Instant timestamp = Instant.now();

}
