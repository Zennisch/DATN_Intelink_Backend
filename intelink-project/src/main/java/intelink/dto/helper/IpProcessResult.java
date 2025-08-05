package intelink.dto.helper;

import intelink.models.enums.IpVersion;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IpProcessResult {
    private IpVersion ipVersion;
    private String ipAddress;
    private String ipNormalized;
    private String subnet;
    private boolean isPrivate;
}
