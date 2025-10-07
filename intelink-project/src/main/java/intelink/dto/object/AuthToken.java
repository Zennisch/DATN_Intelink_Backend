package intelink.dto.object;

import intelink.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthToken {
    private User user;
    private String token;
    private String refreshToken;
    private Long expiresAt;
}
