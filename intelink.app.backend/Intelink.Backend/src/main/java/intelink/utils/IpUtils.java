package intelink.utils;

import intelink.models.enums.IpVersion;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
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
            "^([0-9a-fA-F]{1,4}:){1,7}[0-9a-fA-F]{1,4}$|^::1$|^::$|^(([0-9a-fA-F]{1,4}:){0,6}([0-9a-fA-F]{1,4})?::([0-9a-fA-F]{1,4}:){0,6}([0-9a-fA-F]{1,4})?)$"
    );

    @Data
    @Builder
    public static class IpProcessingResult {
        private String originalIp;
        private IpVersion ipVersion;
        private String normalizedIp;
        private String subnet;
        private boolean isPrivate;
    }

    /**
     * Gets client IP address from request, processes it, and returns complete information
     */
    public static IpProcessingResult processClientIp(HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        return processIp(ipAddress);
    }

    /**
     * Process any IP address to extract all relevant information
     */
    public static IpProcessingResult processIp(String ip) {
        if (!isValidIp(ip)) {
            return IpProcessingResult.builder()
                    .originalIp(ip)
                    .ipVersion(IpVersion.UNKNOWN)
                    .normalizedIp("unknown")
                    .subnet("unknown")
                    .isPrivate(false)
                    .build();
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            IpVersion version;
            String normalizedIp;
            String subnet;
            boolean isPrivate = isPrivateIp(ip);

            if (inetAddress instanceof Inet4Address) {
                version = IpVersion.IPv4;
                normalizedIp = inetAddress.getHostAddress();

                // Create subnet like 192.168.1.0/24
                String[] parts = normalizedIp.split("\\.");
                subnet = parts[0] + "." + parts[1] + "." + parts[2] + ".0/24";
            } else if (inetAddress instanceof Inet6Address) {
                version = IpVersion.IPv6;
                // Normalize IPv6 to standard compressed format
                normalizedIp = inetAddress.getHostAddress();

                // Create /64 subnet (standard network prefix length for IPv6)
                int endOfNetworkPortion = normalizedIp.lastIndexOf(':');
                for (int i = 0; i < 3; i++) {
                    endOfNetworkPortion = normalizedIp.lastIndexOf(':', endOfNetworkPortion - 1);
                    if (endOfNetworkPortion < 0) break;
                }

                if (endOfNetworkPortion > 0) {
                    subnet = normalizedIp.substring(0, endOfNetworkPortion + 1) + ":/64";
                } else {
                    subnet = normalizedIp + "/64";
                }
            } else {
                version = IpVersion.UNKNOWN;
                normalizedIp = ip;
                subnet = "unknown";
            }

            return IpProcessingResult.builder()
                    .originalIp(ip)
                    .ipVersion(version)
                    .normalizedIp(normalizedIp)
                    .subnet(subnet)
                    .isPrivate(isPrivate)
                    .build();
        } catch (UnknownHostException e) {
            log.warn("Failed to process IP address: {}", ip, e);
            return IpProcessingResult.builder()
                    .originalIp(ip)
                    .ipVersion(IpVersion.UNKNOWN)
                    .normalizedIp(ip)
                    .subnet("unknown")
                    .isPrivate(false)
                    .build();
        }
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        log.info("=== IP Address Detection Start ===");
        log.info("RemoteAddr from request: {}", request.getRemoteAddr());

        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            log.info("Request Headers:");
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                log.info("  {}: {}", headerName, headerValue);
            }
        }

        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null) {
                log.debug("Header [{}] contains IP(s): {}", header, ip);
                // Có thể có nhiều IP trong 1 header, lấy IP đầu tiên
                String[] parts = ip.split(",");
                for (String part : parts) {
                    String candidate = part.trim();
                    log.debug("  -> Checking candidate IP: {}", candidate);
                    if (isValidIp(candidate)) {
                        log.info("✔ Valid IP [{}] found in header [{}]", candidate, header);
                        return candidate;
                    } else {
                        log.debug("✘ Invalid or filtered IP: {}", candidate);
                    }
                }
            }
        }

        // Fallback
        String remoteAddr = request.getRemoteAddr();
        if (isValidIp(remoteAddr)) {
            log.info("✔ Valid fallback remoteAddr IP: {}", remoteAddr);
            return remoteAddr;
        } else {
            log.warn("✘ Fallback remoteAddr is invalid: {}", remoteAddr);
        }

        log.warn("Could not determine client IP address.");
        log.info("=== IP Address Detection End ===");
        return "unknown";
    }

    public static boolean isValidIp(String ip) {
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }

        ip = ip.trim();

        // Nếu đang debug local thì cho phép các IP loopback
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip) || "127.0.0.1".equals(ip)) {
            // return false; // Chặn nếu production
            return true; // Cho phép khi debug local
        }

        return IPV4_PATTERN.matcher(ip).matches() || IPV6_PATTERN.matcher(ip).matches();
    }

    public static IpVersion getIpVersion(String ip) {
        if (!isValidIp(ip)) {
            return IpVersion.UNKNOWN;
        }

        if (IPV4_PATTERN.matcher(ip).matches()) {
            return IpVersion.IPv4;
        } else if (IPV6_PATTERN.matcher(ip).matches()) {
            return IpVersion.IPv6;
        } else {
            return IpVersion.UNKNOWN;
        }
    }

    public static boolean isPrivateIp(String ip) {
        if (!isValidIp(ip)) return false;

        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isSiteLocalAddress() || addr.isLoopbackAddress() || addr.isLinkLocalAddress();
        } catch (UnknownHostException e) {
            log.warn("Could not parse IP address: {}", ip);
            return false;
        }
    }

    public static String normalizeIp(String ip) {
        if (!isValidIp(ip)) {
            return ip;
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Failed to normalize IP: {}", ip, e);
            return ip;
        }
    }

    public static String createSubnet(String ip) {
        IpProcessingResult result = processIp(ip);
        return result.getSubnet();
    }

    public static String getCountryFromIp(String ip) {
        if (!isValidIp(ip) || isPrivateIp(ip)) {
            return "Unknown";
        }

        log.debug("GeoIP lookup for IP: {}", ip);
        return "Unknown"; // Placeholder
    }

    public static String getCityFromIp(String ip) {
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
            String[] parts = ip.split(":");
            if (parts.length >= 2) {
                return parts[0] + ":" + parts[1] + ":****:****:****:****:****:****";
            }
        }

        return "***.***.***";
    }
}