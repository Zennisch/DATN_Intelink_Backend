package intelink.utils;

import org.springframework.util.StringUtils;

public class AccessControlValidationUtil {

    public static void validateCIDR(String cidr) {
        if (!StringUtils.hasText(cidr)) {
            throw new IllegalArgumentException("CIDR cannot be empty");
        }

        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid CIDR format. Expected format: IP/prefix");
        }

        String ip = parts[0];
        String prefix = parts[1];

        if (!isValidIPv4(ip) && !isValidIPv6(ip)) {
            throw new IllegalArgumentException("Invalid IP address in CIDR");
        }

        try {
            int prefixLength = Integer.parseInt(prefix);
            if (isValidIPv4(ip) && (prefixLength < 0 || prefixLength > 32)) {
                throw new IllegalArgumentException("IPv4 prefix must be between 0 and 32");
            }
            if (isValidIPv6(ip) && (prefixLength < 0 || prefixLength > 128)) {
                throw new IllegalArgumentException("IPv6 prefix must be between 0 and 128");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid prefix length in CIDR");
        }
    }

    public static void validateGeography(String countryCode) {
        if (!StringUtils.hasText(countryCode)) {
            throw new IllegalArgumentException("Country code cannot be empty");
        }

        if (!countryCode.matches("^[A-Z]{2}$")) {
            throw new IllegalArgumentException("Invalid country code. Must be 2-letter ISO 3166-1 alpha-2 code (e.g., US, VN, GB)");
        }
    }

    private static boolean isValidIPv4(String ip) {
        String ipv4Pattern = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
        return ip.matches(ipv4Pattern);
    }

    private static boolean isValidIPv6(String ip) {
        String ipv6Pattern = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^::$|^::1$|^([0-9a-fA-F]{1,4}:){1,7}:$|^:(:([0-9a-fA-F]{1,4})){1,7}$|^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$";
        return ip.matches(ipv6Pattern);
    }


}
