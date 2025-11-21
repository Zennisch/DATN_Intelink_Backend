package intelink.utils.helper;

import intelink.models.enums.IPVersion;

public record IpProcessResult(
        IPVersion ipVersion,
        String ipAddress,
        String ipNormalized,
        String subnet,
        Boolean isPrivate
) {
}
