package intelink.dto.object.threat.enums;

public enum PlatformType {
    PLATFORM_TYPE_UNSPECIFIED,
    WINDOWS,
    LINUX,
    ANDROID,
    OSX,
    IOS,
    ANY_PLATFORM,
    ALL_PLATFORMS,
    CHROME;

    public static PlatformType fromString(String value) {
        try {
            return PlatformType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown PlatformType: " + value, e);
        }
    }
}
