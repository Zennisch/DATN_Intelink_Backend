package intelink.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

@Slf4j
public class IpUtils {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^::1$|^::$"
    );

    public static String getClientIpAddress(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // Handle multiple IPs in X-Forwarded-For header
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                if (isValidIp(ip)) {
                    log.debug("Found IP {} in header {}", ip, header);
                    return ip;
                }
            }
        }

        String remoteAddr = request.getRemoteAddr();
        if (isValidIp(remoteAddr)) {
            return remoteAddr;
        }

        log.warn("Could not determine client IP address");
        return "unknown";
    }

    public static boolean isValidIp(String ip) {
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }

        ip = ip.trim();

        // Check for common invalid values
        if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip) || "localhost".equals(ip)) {
            return false;
        }

        return IPV4_PATTERN.matcher(ip).matches() || IPV6_PATTERN.matcher(ip).matches();
    }

    public static boolean isPrivateIp(String ip) {
        if (!isValidIp(ip)) {
            return false;
        }

        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isSiteLocalAddress() ||
                    addr.isLoopbackAddress() ||
                    addr.isLinkLocalAddress();
        } catch (UnknownHostException e) {
            log.warn("Could not parse IP address: {}", ip);
            return false;
        }
    }

    public static String getCountryFromIp(String ip) {
        // This is a placeholder - in production you would integrate with
        // a GeoIP service like MaxMind, IPStack, or similar
        if (!isValidIp(ip) || isPrivateIp(ip)) {
            return "Unknown";
        }

        // Example integration points:
        // - MaxMind GeoLite2 database
        // - REST API calls to GeoIP services
        // - Local GeoIP database lookup

        log.debug("GeoIP lookup for IP: {}", ip);
        return "Unknown"; // Placeholder
    }

    public static String getCityFromIp(String ip) {
        // Similar to getCountryFromIp - placeholder for GeoIP integration
        if (!isValidIp(ip) || isPrivateIp(ip)) {
            return "Unknown";
        }

        log.debug("GeoIP city lookup for IP: {}", ip);
        return "Unknown"; // Placeholder
    }

    public static String maskIpAddress(String ip) {
        if (!isValidIp(ip)) {
            return "***.***.***";
        }

        if (IPV4_PATTERN.matcher(ip).matches()) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + ".***.***";
            }
        } else if (IPV6_PATTERN.matcher(ip).matches()) {
            // Mask IPv6 address
            String[] parts = ip.split(":");
            if (parts.length >= 4) {
                return parts[0] + ":" + parts[1] + ":****:****:****:****:****:****";
            }
        }

        return "***.***.***";
    }
}