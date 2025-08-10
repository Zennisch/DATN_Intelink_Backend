package intelink.dto.object;

import intelink.models.User;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.Authentication;

@Data
@Builder
public class LoginObject {
    private User user;
    private String token;
    private String refreshToken;
    private Long expiresIn;
}
