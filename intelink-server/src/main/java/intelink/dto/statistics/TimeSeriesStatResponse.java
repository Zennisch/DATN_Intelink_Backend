package intelink.dto.statistics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TimeSeriesStatResponse {
    private String granularity;
    private String timezone;
    private String from;
    private String to;
    private Long totalClicks;
    private Long totalAllowedClicks;
    private Long totalBlockedClicks;
    private List<TimeSeriesStatItemResponse> data;
}
