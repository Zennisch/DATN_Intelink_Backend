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
<<<<<<< HEAD
    private Long totalClicks;
=======
>>>>>>> c082875b64e8f460ed7201794b5021d98d090f60

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
