package intelink.modules.api.services;

import intelink.dto.api.ApiKeyResponse;
import intelink.dto.api.CreateApiKeyRequest;
import intelink.models.ApiKey;
import intelink.models.User;
import intelink.modules.api.repositories.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    @Transactional
    public ApiKeyResponse createApiKey(User user, CreateApiKeyRequest request) {
        String rawKey = generateRawKey();
        String keyHash = passwordEncoder.encode(rawKey);
        String keyPrefix = rawKey.substring(0, 10);

        ApiKey apiKey = ApiKey.builder()
                .user(user)
                .name(request.getName())
                .rawKey(rawKey)
                .keyHash(keyHash)
                .keyPrefix(keyPrefix)
                .rateLimitPerHour(1000)
                .active(true)
                .build();

        apiKey = apiKeyRepository.save(apiKey);

        return ApiKeyResponse.fromEntity(apiKey, rawKey);
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> getApiKeys(User user) {
        return apiKeyRepository.findByUser(user).stream()
                .map(key -> ApiKeyResponse.fromEntity(key, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteApiKey(User user, UUID id) {
        ApiKey apiKey = apiKeyRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("API Key not found"));
        apiKeyRepository.delete(apiKey);
    }

    private String generateRawKey() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return "sk_" + base64Encoder.encodeToString(randomBytes);
    }
}
