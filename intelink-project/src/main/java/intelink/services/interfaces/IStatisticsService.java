package intelink.services.interfaces;

import intelink.dto.response.stat.StatisticsResponse;
import intelink.dto.response.stat.TimeStatsResponse;
import intelink.dto.response.stat.TopPeakTimesResponse;

import java.util.Map;

public interface IStatisticsService {
    Map<String, Object> getDeviceStats(String shortCode);

    Map<String, Object> getLocationStats(String shortCode);

    TimeStatsResponse getTimeStats(String shortCode, String customFrom, String customTo, String granularityStr);

    StatisticsResponse getDimensionStats(String shortCode, String type);

    Map<String, Object> getPeakTimeStats(String shortCode, String customFrom, String customTo, String granularityStr);

    TopPeakTimesResponse getTopPeakTimes(String shortCode, String granularityStr);
}