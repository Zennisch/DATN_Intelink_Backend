package intelink.dto.url;

import intelink.models.ShortUrl;
import intelink.models.ShortUrlAccessControl;
import intelink.models.enums.AccessControlMode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CreateShortUrlResponse {

    private Long id;
    private String title;
    private String description;
    private String originalUrl;
    private String shortCode;
    private String shortUrl;
    private Boolean enabled;
    private Integer maxUsage;
    private Instant expiresAt;
    private Long totalClicks;
    private AccessControlMode accessControlMode;
    private Long allowedClicks;
    private Long blockedClicks;
    private Long uniqueClicks;
    private Instant createdAt;
    private Instant updatedAt;

    private Boolean hasPassword;
    private List<String> accessControlCIDRs;
    private List<String> accessControlGeographies;

    public static CreateShortUrlResponse fromEntity(
            ShortUrl shortUrl,
            List<ShortUrlAccessControl> accessControls,
            String accessUrlTemplate
    ) {
        boolean hasPassword = false;
        List<String> cidrs = new ArrayList<>();
        List<String> geographies = new ArrayList<>();

        if (accessControls != null) {
            for (ShortUrlAccessControl control : accessControls) {
                switch (control.getType()) {
                    case PASSWORD_PROTECTED:
                        hasPassword = true;
                        break;
                    case CIDR:
                        cidrs.add(control.getValue());
                        break;
                    case GEOGRAPHY:
                        geographies.add(control.getValue());
                        break;
                }
            }
        }

        String fullShortUrl = accessUrlTemplate.replace("{shortCode}", shortUrl.getShortCode());

        return CreateShortUrlResponse.builder()
                .id(shortUrl.getId())
                .title(shortUrl.getTitle())
                .description(shortUrl.getDescription())
                .originalUrl(shortUrl.getOriginalUrl())
                .shortCode(shortUrl.getShortCode())
                .shortUrl(fullShortUrl)
                .enabled(shortUrl.getEnabled())
                .maxUsage(shortUrl.getMaxUsage())
                .expiresAt(shortUrl.getExpiresAt())
                .totalClicks(shortUrl.getTotalClicks())
                .accessControlMode(shortUrl.getAccessControlMode())
                .allowedClicks(shortUrl.getAllowedClicks())
                .blockedClicks(shortUrl.getBlockedClicks())
                .uniqueClicks(shortUrl.getUniqueClicks())
                .createdAt(shortUrl.getCreatedAt())
                .updatedAt(shortUrl.getUpdatedAt())
                .hasPassword(hasPassword)
                .accessControlCIDRs(cidrs.isEmpty() ? null : cidrs)
                .accessControlGeographies(geographies.isEmpty() ? null : geographies)
                .build();
    }
}
