package intelink.utils.helper;

import intelink.models.enums.IPVersion;

public record IpInfo(
        IPVersion ipVersion,
        String ipAddress,
        String ipNormalized,
        String subnet,
        Boolean isPrivate
) {
}
