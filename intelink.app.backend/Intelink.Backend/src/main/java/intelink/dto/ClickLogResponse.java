package intelink.dto;

import intelink.models.ClickLog;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ClickLogResponse {

    private UUID id;
    private String shortCode;
    private String ipAddress;
    private String country;
    private String city;
    private String browser;
    private String os;
    private String deviceType;
    private String referrer;
    private Instant timestamp;

    public static ClickLogResponse fromEntity(ClickLog clickLog) {
        return ClickLogResponse.builder()
                .id(clickLog.getId())
                .shortCode(clickLog.getShortCode())
                .ipAddress(maskIpAddress(clickLog.getIpAddress()))
                .country(clickLog.getCountry())
                .city(clickLog.getCity())
                .browser(clickLog.getBrowser())
                .os(clickLog.getOs())
                .deviceType(clickLog.getDeviceType())
                .referrer(clickLog.getReferrer())
                .timestamp(clickLog.getTimestamp())
                .build();
    }

    private static String maskIpAddress(String ipAddress) {
        if (ipAddress == null) return null;
        String[] parts = ipAddress.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".***.***";
        }
        return "***.***.***";
    }
}