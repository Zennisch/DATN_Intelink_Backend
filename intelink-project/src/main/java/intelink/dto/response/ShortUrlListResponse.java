package intelink.dto.response;

import intelink.models.ShortUrl;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ShortUrlListResponse {

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
    private String shortUrl;

    public static ShortUrlListResponse fromEntity(ShortUrl shortUrl, String baseUrl) {
        return ShortUrlListResponse.builder()
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
                .shortUrl(baseUrl + "/" + shortUrl.getShortCode())
                .build();
    }

    public static List<ShortUrlListResponse> fromEntities(List<ShortUrl> shortUrls, String baseUrl) {
        return shortUrls.stream()
                .map(shortUrl -> fromEntity(shortUrl, baseUrl))
                .toList();
    }
}
