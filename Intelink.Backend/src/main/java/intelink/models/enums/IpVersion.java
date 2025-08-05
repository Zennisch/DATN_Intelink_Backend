package intelink.models.enums;

public enum IpVersion {
    IPv4, IPv6, UNKNOWN;

    public static IpVersion fromString(String version) {
        try {
            return IpVersion.valueOf(version.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
