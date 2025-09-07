package intelink.dto.response.auth;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private Long totalClicks;
    private Integer totalShortUrls;
    private Boolean emailVerified;
    private String authProvider;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;
}