package intelink.dto.response.url;

import intelink.models.ShortUrl;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CreateShortUrlResponse {

    private Long id;
    private String shortCode;
    private String originalUrl;
    private Boolean hasPassword;
    private String description;
    private String status;
    private Long maxUsage;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
    private String shortUrl;

    public static CreateShortUrlResponse fromEntity(ShortUrl shortUrl, String baseUrl) {
        return CreateShortUrlResponse.builder()
                .id(shortUrl.getId())
                .shortCode(shortUrl.getShortCode())
                .originalUrl(shortUrl.getOriginalUrl())
                .hasPassword(shortUrl.getPasswordHash() != null)
                .description(shortUrl.getDescription())
                .status(shortUrl.getStatus().name())
                .maxUsage(shortUrl.getMaxUsage())
                .expiresAt(shortUrl.getExpiresAt())
                .createdAt(shortUrl.getCreatedAt())
                .updatedAt(shortUrl.getUpdatedAt())
                .shortUrl(baseUrl.replace("{shortCode}", shortUrl.getShortCode()))
                .build();
    }

}
