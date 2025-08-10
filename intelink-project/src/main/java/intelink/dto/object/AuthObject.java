package intelink.dto.object;

import intelink.models.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthObject {
    private User user;
    private String token;
    private String refreshToken;
    private Long expiresAt;
}
