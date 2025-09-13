package intelink.services.interfaces;

import intelink.dto.object.AuthToken;

public interface IOAuthService {
    AuthToken callback(String authToken);
}
