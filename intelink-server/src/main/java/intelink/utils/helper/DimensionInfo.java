package intelink.utils.helper;

public record DimensionInfo(
        String country,
        String countryCode,
        String city,
        String browser,
        String os,
        String deviceType
) {
}