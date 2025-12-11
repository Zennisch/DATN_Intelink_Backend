package intelink.dto.statistics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GeographyStatResponse {
    private String category;
    private Long totalClicks;
    private Long totalAllowedClicks;
    private Long totalBlockedClicks;
    private List<GeographyStatItemResponse> data;
}
