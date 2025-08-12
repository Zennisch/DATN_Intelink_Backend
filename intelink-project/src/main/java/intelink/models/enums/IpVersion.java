package intelink.models.enums;

public enum IpVersion {
    IPV4, IPV6, UNKNOWN;

    public static IpVersion fromString(String version) {
        try {
            return IpVersion.valueOf(version.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid IP version: " + version, e);
        }
    }
}
