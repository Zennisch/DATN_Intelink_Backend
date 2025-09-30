package intelink.dto.response.api_key;

import intelink.models.ApiKey;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ApiKeyResponse {
    private UUID id;
    private String name;
    private String rawKey;
    private String keyPrefix;
    private Integer rateLimitPerHour;
    private Boolean active;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastUsedAt;

    public static ApiKeyResponse fromEntity(ApiKey apiKey) {
        ApiKeyResponse response = new ApiKeyResponse();
        response.setId(apiKey.getId());
        response.setName(apiKey.getName());
        response.setRawKey(apiKey.getRawKey());
        response.setKeyPrefix(apiKey.getKeyPrefix());
        response.setRateLimitPerHour(apiKey.getRateLimitPerHour());
        response.setActive(apiKey.getActive());
        response.setExpiresAt(apiKey.getExpiresAt());
        response.setCreatedAt(apiKey.getCreatedAt());
        response.setUpdatedAt(apiKey.getUpdatedAt());
        response.setLastUsedAt(apiKey.getLastUsedAt());
        return response;
    }
}
