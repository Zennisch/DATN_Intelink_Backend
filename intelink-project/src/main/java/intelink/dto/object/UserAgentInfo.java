package intelink.dto.object;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAgentInfo {
    String browser;
    String os;
    String deviceType;
}
