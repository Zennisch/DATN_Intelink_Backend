package intelink.dto.request.url;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchCreateShortUrlRequest {
    @NotEmpty
    private List<CreateShortUrlRequest> requests;
}