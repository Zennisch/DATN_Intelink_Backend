package intelink.utils;

import intelink.dto.object.IpProcessResult;
import intelink.models.enums.IpVersion;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class IpUtil {

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

    public static IpProcessResult process(HttpServletRequest request) {
        String ip = getClientIpAddress(request);
        if (!isValidIp(ip)) {
            return IpProcessResult.builder()
                    .ipVersion(IpVersion.UNKNOWN)
                    .ipAddress(ip)
                    .ipNormalized(null)
                    .subnet(null)
                    .isPrivate(false)
                    .build();
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            IpVersion ipVersion;
            String ipNormalized;
            String subnet;
            boolean isPrivate = isPrivateIp(inetAddress);

            if (inetAddress instanceof Inet4Address) {
                ipVersion = IpVersion.IPv4;
                ipNormalized = inetAddress.getHostAddress();
                String[] parts = ipNormalized.split("\\.");
                subnet = parts[0] + "." + parts[1] + "." + parts[2] + ".0/24";
            } else if (inetAddress instanceof Inet6Address) {
                ipVersion = IpVersion.IPv6;
                ipNormalized = inetAddress.getHostAddress();

                int endOfNetworkPortion = ipNormalized.lastIndexOf(':');
                for (int i = 0; i < 3; i++) {
                    endOfNetworkPortion = ipNormalized.lastIndexOf(':', endOfNetworkPortion - 1);
                    if (endOfNetworkPortion < 0) break;
                }

                if (endOfNetworkPortion > 0) {
                    subnet = ipNormalized.substring(0, endOfNetworkPortion + 1) + ":/64";
                } else {
                    subnet = ipNormalized + "/64";
                }
            } else {
                ipVersion = IpVersion.UNKNOWN;
                ipNormalized = null;
                subnet = null;
            }
            return IpProcessResult.builder()
                    .ipVersion(ipVersion)
                    .ipAddress(ip)
                    .ipNormalized(ipNormalized)
                    .subnet(subnet)
                    .isPrivate(isPrivate)
                    .build();
        } catch (Exception e) {
            log.error("IpUtil.process - Error processing IP: {} - {}", ip, e.getMessage());
            return IpProcessResult.builder()
                    .ipVersion(IpVersion.UNKNOWN)
                    .ipAddress(ip)
                    .ipNormalized(null)
                    .subnet(null)
                    .isPrivate(false)
                    .build();
        }
    }

    private static String getClientIpAddress(HttpServletRequest request) {
        log.debug("IpUtil.getClientIpAddress - Attempting to retrieve client IP address from request headers");
        List<String> headerNames = Collections.list(request.getHeaderNames());

        if (!headerNames.isEmpty()) {
            log.debug("Request Headers:");
            headerNames.forEach(headerName -> {
                String headerValue = request.getHeader(headerName);
                log.debug("  {}: {}", headerName, headerValue);
            });
        }

        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip == null || ip.isEmpty()) {
                continue;
            }
            String[] parts = ip.split(",");
            for (String part : parts) {
                String candidateIp = part.trim();
                log.debug("IpUtil.getClientIpAddress - Checking IP candidate from header [{}]: {}", header, candidateIp);
                if (isValidIp(candidateIp)) {
                    log.debug("IpUtil.getClientIpAddress - Valid IP [{}] found in header [{}]", candidateIp, header);
                    return candidateIp;
                } else {
                    log.debug("IpUtil.getClientIpAddress - Invalid or filtered IP: {}", candidateIp);
                }
            }
        }

        String remoteAddr = request.getRemoteAddr();
        log.debug("IpUtil.getClientIpAddress - Remote address from request: {}", remoteAddr);
        if (isValidIp(remoteAddr)) {
            log.debug("IpUtil.getClientIpAddress - Valid fallback remoteAddr IP: {}", remoteAddr);
            return remoteAddr;
        } else {
            log.warn("IpUtil.getClientIpAddress - Fallback remoteAddr is invalid: {}", remoteAddr);
            return null;
        }
    }

    private static boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip) || "127.0.0.1".equals(ip)) {
            return true;
        }

        return IPV4_PATTERN.matcher(ip).matches() || IPV6_PATTERN.matcher(ip).matches();
    }

    private static boolean isPrivateIp(InetAddress inetAddress) {
        return inetAddress.isSiteLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress();
    }

}
