package intelink.services.interfaces;

import intelink.dto.response.stat.StatisticsResponse;
import intelink.dto.response.stat.TimeStatsResponse;

import java.util.Map;

public interface IStatisticsService {
    Map<String, Object> getDeviceStats(String shortCode);

    Map<String, Object> getLocationStats(String shortCode);

    TimeStatsResponse getTimeStats(String shortCode, String customFrom, String customTo, String granularityStr);

    StatisticsResponse getDimensionStats(String shortCode, String type);
}