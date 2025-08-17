package intelink.services.interfaces;

import intelink.dto.response.StatisticsResponse;
import intelink.dto.response.TimeStatsResponse;
import intelink.models.enums.DimensionType;

<<<<<<< HEAD
import java.time.Instant;
import java.util.ArrayList;
=======
>>>>>>> c082875b64e8f460ed7201794b5021d98d090f60
import java.util.Map;

public interface IStatisticsService {
    Map<String, Object> getDeviceStats(String shortCode);

    Map<String, Object> getLocationStats(String shortCode);

    TimeStatsResponse getTimeStats(String shortCode, String customFrom, String customTo);

    StatisticsResponse getDimensionStats(String shortCode, String type);
}
