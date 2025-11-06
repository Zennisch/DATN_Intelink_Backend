// java
package intelink.dto.response.stat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSeriesAggregateResponse {
    private String granularity;
    private String from;
    private String to;
    private Long totalViews;
    private List<TimeSeriesPoint> data;
}