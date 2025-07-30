package intelink.services.interfaces;

import intelink.dto.helper.DimensionInfo;
import intelink.models.enums.DimensionType;

public interface IAnalyticService {

    void recordDimensionStats(String shortCode, DimensionInfo dimensionInfo);

}
