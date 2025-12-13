package intelink.dto.api;

import intelink.models.ApiKey;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ApiKeyResponse {
    private UUID id;
    private String name;
    private String keyPrefix;
    private String rawKey; // Only returned on creation
    private Integer rateLimitPerHour;
    private Boolean active;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant lastUsedAt;

    public static ApiKeyResponse fromEntity(ApiKey apiKey, String rawKey) {
        return ApiKeyResponse.builder()
                .id(apiKey.getId())
                .name(apiKey.getName())
                .keyPrefix(apiKey.getKeyPrefix())
                .rawKey(rawKey) // Can be null if not just created
                .rateLimitPerHour(apiKey.getRateLimitPerHour())
                .active(apiKey.getActive())
                .expiresAt(apiKey.getExpiresAt())
                .createdAt(apiKey.getCreatedAt())
                .lastUsedAt(apiKey.getLastUsedAt())
                .build();
    }
}
