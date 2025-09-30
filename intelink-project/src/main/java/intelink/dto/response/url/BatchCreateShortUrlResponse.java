package intelink.dto.response.url;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BatchCreateShortUrlResponse {
    private List<CreateShortUrlResponse> results;
}