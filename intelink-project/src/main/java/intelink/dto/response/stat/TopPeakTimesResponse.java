package intelink.dto.response.stat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopPeakTimesResponse {
    private String granularity;
    private int total;
    private List<PeakTime> topPeakTimes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeakTime {
        private String time; // ISO string
        private long clicks;
    }
}
