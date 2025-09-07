package intelink.services.interfaces;

import intelink.dto.object.DimensionInfo;


public interface IAnalyticService {

    void recordDimensionStats(String shortCode, DimensionInfo dimensionInfo);

    void recordClickStats(String shortCode);
}
