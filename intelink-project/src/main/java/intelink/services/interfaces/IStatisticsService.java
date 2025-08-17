package intelink.services.interfaces;

import intelink.dto.response.StatisticsResponse;
import intelink.dto.response.TimeStatsResponse;
import intelink.models.enums.DimensionType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;

public interface IStatisticsService {
    Map<String, Object> getDeviceStats(String shortCode);

    Map<String, Object> getLocationStats(String shortCode);

    TimeStatsResponse getTimeStats(String shortCode, String customFrom, String customTo);

    StatisticsResponse getDimensionStats(String shortCode, String type);
}
