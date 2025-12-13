package intelink.dto.statistics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeSeriesStatItemResponse {
    private String bucketStart;
    private String bucketEnd;
    private Long clicks;
    private Long allowedClicks;
    private Long blockedClicks;
}
