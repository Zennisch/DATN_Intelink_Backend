package intelink.services;

import intelink.models.ApiKey;
import intelink.repositories.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKey validateAndGetApiKey(String key) {
        Optional<ApiKey> apiKey = apiKeyRepository.findByKeyHash(key);
        if (apiKey.isPresent() && apiKey.get().isUsable()) {
            return apiKey.get();
        }
        return null;
    }

    public void saveLastUsed(ApiKey apiKey) {
        apiKey.updateLastUsed();
        apiKeyRepository.save(apiKey);
    }

}
