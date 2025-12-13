package intelink.dto.statistics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PeakTimeStatResponse {
    private String granularity;
    private String timezone;
    private String from;
    private String to;
    private Integer totalBuckets;
    private Integer returnedBuckets;
    private List<TimeSeriesStatItemResponse> data;
}
