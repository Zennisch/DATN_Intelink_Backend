package intelink.services.interfaces;

import intelink.models.enums.DimensionType;

import java.util.Map;

public interface IStatisticsService {
    Map<String, Object> getDeviceStats(String shortCode);

    Map<String, Object> getLocationStats(String shortCode);

    Map<String, Object> getTimeStats(String shortCode);

    Map<String, Object> getDimensionStats(String shortCode, DimensionType type);
}
