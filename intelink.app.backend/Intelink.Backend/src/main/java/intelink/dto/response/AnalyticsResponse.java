package intelink.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AnalyticsResponse {

    private String shortCode;
    private String type;
    private Map<String, Long> data;
    private Long totalCount;
    private String period;
}