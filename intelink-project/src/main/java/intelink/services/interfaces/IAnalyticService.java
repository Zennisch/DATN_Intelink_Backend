package intelink.services.interfaces;

import intelink.dto.helper.DimensionInfo;
import intelink.models.enums.DimensionType;

import java.util.ArrayList;
import java.util.Map;

public interface IAnalyticService {

    void recordDimensionStats(String shortCode, DimensionInfo dimensionInfo);

    void recordClickStats(String shortCode);

    ArrayList<Map<String, Object>> getDeviceStats(String shortCode);

    ArrayList<Map<String, Object>> getLocationStats(String shortCode);

    ArrayList<Map<String, Object>> getTimeStats(String shortCode);

    ArrayList<Map<String, Object>> getDimensionStats(String shortCode, DimensionType type);
}
