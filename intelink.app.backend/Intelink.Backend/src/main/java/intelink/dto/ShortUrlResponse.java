package intelink.dto;

import intelink.models.ShortUrl;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ShortUrlResponse {

    private Long id;
    private String shortCode;
    private String originalUrl;
    private String description;
    private Instant expiresAt;
    private Long maxUsage;
    private Boolean hasPassword;
    private Boolean isActive;
    private Long totalClicks;
    private Instant createdAt;
    private Instant updatedAt;
    private String shortUrl;

    public static ShortUrlResponse fromEntity(ShortUrl shortUrl) {
        return ShortUrlResponse.builder()
                .id(shortUrl.getId())
                .shortCode(shortUrl.getShortCode())
                .originalUrl(shortUrl.getOriginalUrl())
                .description(shortUrl.getDescription())
                .expiresAt(shortUrl.getExpiresAt() != null ? shortUrl.getExpiresAt().toInstant() : null)
                .maxUsage(shortUrl.getMaxUsage())
                .hasPassword(shortUrl.getPassword() != null)
                .isActive(shortUrl.getIsActive())
                .totalClicks(shortUrl.getTotalClicks())
                .createdAt(shortUrl.getCreatedAt())
                .updatedAt(shortUrl.getUpdatedAt())
                .shortUrl("https://intelink.app/" + shortUrl.getShortCode())
                .build();
    }
}