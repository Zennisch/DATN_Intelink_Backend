package intelink.dto.url;

import intelink.models.ShortUrl;
import intelink.models.ShortUrlAccessControl;
import intelink.models.ShortUrlAnalysisResult;
import intelink.models.enums.AccessControlMode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class UpdateShortUrlResponse {

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
    private List<ShortUrlAnalysisResultResponse> analysisResults;

    public static UpdateShortUrlResponse fromEntity(
            ShortUrl shortUrl,
            List<ShortUrlAccessControl> accessControls,
            String accessUrlTemplate
    ) {
        return fromEntity(shortUrl, accessControls, accessUrlTemplate, null);
    }

    public static UpdateShortUrlResponse fromEntity(
            ShortUrl shortUrl,
            List<ShortUrlAccessControl> accessControls,
            String accessUrlTemplate,
            List<ShortUrlAnalysisResult> analysisResults
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

        List<ShortUrlAnalysisResultResponse> analysisResponses = null;
        if (analysisResults != null && !analysisResults.isEmpty()) {
            analysisResponses = analysisResults.stream()
                    .map(ShortUrlAnalysisResultResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        return UpdateShortUrlResponse.builder()
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
                .analysisResults(analysisResponses)
                .build();
    }
}
