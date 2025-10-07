package intelink.services;

import intelink.dto.request.api_key.CreateApiKeyRequest;
import intelink.dto.response.api_key.ApiKeyResponse;
import intelink.models.ApiKey;
import intelink.models.User;
import intelink.repositories.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    @Transactional
    public ApiKey validateAndGetApiKey(String key) {
        Optional<ApiKey> apiKey = apiKeyRepository.findByRawKey(key);
        if (apiKey.isPresent() && apiKey.get().isUsable()) {
            return apiKey.get();
        }
        return null;
    }

    @Transactional
    public void saveLastUsed(ApiKey apiKey) {
        apiKey.updateLastUsed();
        apiKeyRepository.save(apiKey);
    }

    @Transactional
    public List<ApiKeyResponse> listByUser(User user) {
        return apiKeyRepository.findByUser(user).stream()
                .map(ApiKeyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<ApiKeyResponse> getById(UUID id, User user) {
        return apiKeyRepository.findById(id)
                .filter(apiKey -> apiKey.getUser().getId().equals(user.getId()))
                .map(ApiKeyResponse::fromEntity);
    }

    private String generateApiKey() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash API key", e);
        }
    }

    @Transactional
    public ApiKeyResponse create(User user, CreateApiKeyRequest request) {
        String rawKey = generateApiKey();
        String keyPrefix = rawKey.substring(0, 8);
        String keyHash = hashKey(rawKey);

        ApiKey apiKey = ApiKey.builder()
                .user(user)
                .name(request.getName())
                .rawKey(rawKey)
                .keyPrefix(keyPrefix)
                .keyHash(keyHash)
                .rateLimitPerHour(request.getRateLimitPerHour() != null ? request.getRateLimitPerHour() : 1000)
                .active(request.getActive() != null ? request.getActive() : true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        apiKey = apiKeyRepository.save(apiKey);

        return ApiKeyResponse.fromEntity(apiKey);
        // Optionally, you can return the raw key to the user ONCE here
        // dto.setRawKey(rawKey);
    }

    @Transactional
    public Optional<ApiKeyResponse> update(UUID id, User user, CreateApiKeyRequest request) {
        return apiKeyRepository.findById(id)
                .filter(apiKey -> apiKey.getUser().getId().equals(user.getId()))
                .map(apiKey -> {
                    apiKey.setName(request.getName());
                    if (request.getRateLimitPerHour() != null) {
                        apiKey.setRateLimitPerHour(request.getRateLimitPerHour());
                    }
                    if (request.getActive() != null) {
                        apiKey.setActive(request.getActive());
                    }

                    apiKey.setUpdatedAt(Instant.now());
                    ApiKey updated = apiKeyRepository.save(apiKey);
                    return ApiKeyResponse.fromEntity(updated);
                });
    }

    @Transactional
    public boolean delete(UUID id, User user) {
        return apiKeyRepository.findById(id)
                .filter(apiKey -> apiKey.getUser().getId().equals(user.getId()))
                .map(apiKey -> {
                    apiKeyRepository.delete(apiKey);
                    return true;
                }).orElse(false);
    }

    @Transactional
    public User getUserByApiKey(String key) {
        Optional<ApiKey> apiKey = apiKeyRepository.findByRawKey(key);
        return apiKey.map(ApiKey::getUser).orElse(null);
    }

    @Transactional(readOnly = true)
    public String getUsernameByApiKey(String key) {
        return apiKeyRepository.findByRawKey(key)
                .map(apiKey -> apiKey.getUser().getUsername())
                .orElse(null);
    }
}
