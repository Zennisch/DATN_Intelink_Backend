package intelink.helper;

import intelink.models.enums.IPVersion;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IpProcessResult {
    private IPVersion ipVersion;
    private String ipAddress;
    private String ipNormalized;
    private String subnet;
    private Boolean isPrivate;
}
