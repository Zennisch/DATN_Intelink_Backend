package intelink.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class StatsResponse {

    private String shortCode;
    private Long totalClicks;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DailyClicksDto> dailyStats;
    private Map<String, Long> topCountries;
    private Map<String, Long> topBrowsers;
    private Map<String, Long> deviceTypes;
    private int[] hourlyClicks;

    @Data
    @Builder
    public static class DailyClicksDto {
        private LocalDate date;
        private Long clicks;
    }
}