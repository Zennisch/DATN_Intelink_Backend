package intelink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private String shortCode;
    private String category;
    private List<StatData> data;
    private Long totalClicks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatData {
        private String name;
        private Long clicks;
        private Double percentage;
    }
}
