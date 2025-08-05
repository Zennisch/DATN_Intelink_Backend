package intelink.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private Long totalClicks;
    private Integer totalShortUrls;
    private Instant createdAt;
    private Instant updatedAt;

}
