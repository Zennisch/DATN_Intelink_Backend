package intelink.dto.response.url;

import intelink.models.ShortUrl;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ShortUrlDetailResponse {

    private Long id;
    private String shortCode;
    private String originalUrl;
    private Boolean hasPassword;
    private String description;
    private String status;
    private Long maxUsage;
    private Long totalClicks;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
    private String shortUrl;

    public static ShortUrlDetailResponse fromEntity(ShortUrl shortUrl, String baseUrl) {
        return ShortUrlDetailResponse.builder()
                .id(shortUrl.getId())
                .shortCode(shortUrl.getShortCode())
                .originalUrl(shortUrl.getOriginalUrl())
                .hasPassword(shortUrl.getPassword() != null)
                .description(shortUrl.getDescription())
                .status(shortUrl.getStatus().toString())
                .maxUsage(shortUrl.getMaxUsage())
                .totalClicks(shortUrl.getTotalClicks())
                .expiresAt(shortUrl.getExpiresAt())
                .createdAt(shortUrl.getCreatedAt())
                .updatedAt(shortUrl.getUpdatedAt())
                .shortUrl(baseUrl + "/" + shortUrl.getShortCode())
                .build();
    }
}
