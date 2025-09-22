package intelink.services.interfaces;

import intelink.dto.object.DimensionInfo;
import intelink.dto.response.redirect.RedirectResult;
import jakarta.servlet.http.HttpServletRequest;

public interface IRedirectService {

    RedirectResult handleRedirect(String shortCode, String password, HttpServletRequest request);

}