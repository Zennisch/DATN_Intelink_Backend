package intelink.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import intelink.dto.object.Auth;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String username;
    private String email;
    private String role;
    private Long expiresAt;

    public static AuthResponse fromEntity(Auth obj) {
        return AuthResponse.builder()
                .token(obj.getToken())
                .refreshToken(obj.getRefreshToken())
                .username(obj.getUser().getUsername())
                .email(obj.getUser().getEmail())
                .role(obj.getUser().getRole().toString())
                .expiresAt(obj.getExpiresAt())
                .build();
    }

}
