package intelink.dto.object;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenObject {
    private String token;
    private String username;
}
