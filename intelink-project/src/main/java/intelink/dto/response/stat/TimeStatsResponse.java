package intelink.dto.response.stat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeStatsResponse {
    private String granularity;
    private String from;
    private String to;
    private long totalClicks;
    private List<Bucket> buckets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Bucket {
        private String time; // ISO string
        private long clicks;
    }
}




