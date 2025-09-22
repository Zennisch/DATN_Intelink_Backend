package intelink.services.interfaces;

import intelink.dto.object.DimensionInfo;
import intelink.models.ClickLog;
import jakarta.servlet.http.HttpServletRequest;

public interface IClickLogService {

    ClickLog record(String shortCode, HttpServletRequest request);

    void recordDimensionStats(String shortCode, DimensionInfo dimensionInfo);

    void recordClickStats(String shortCode);

}
