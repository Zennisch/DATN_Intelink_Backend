package intelink.services.interfaces;

import intelink.models.ClickLog;
import jakarta.servlet.http.HttpServletRequest;

public interface IClickLogService {

    ClickLog record(String shortCode, HttpServletRequest request);

}
