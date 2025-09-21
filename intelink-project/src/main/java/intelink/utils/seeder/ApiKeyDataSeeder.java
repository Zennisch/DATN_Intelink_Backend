package intelink.utils.seeder;

import intelink.models.ApiKey;
import intelink.models.User;
import intelink.repositories.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyDataSeeder {

    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSeedingUtils utils;

    public void createApiKeys(List<User> users, int count) {
        log.info("Creating {} API keys...", count);
        List<ApiKey> apiKeys = new ArrayList<>();

        String[] keyNames = {
                "Production API", "Development API", "Testing API", "Mobile App API",
                "Web App API", "Analytics API", "Webhook API", "Integration API"
        };

        for (int i = 0; i < count; i++) {
            User randomUser = utils.getRandomElement(users);
            String rawKey = "ik_" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);
            String keyHash = passwordEncoder.encode(rawKey);
            String keyPrefix = rawKey.substring(0, 8);
            Instant createdAt = utils.getRandomInstantBetween(2024, 2025);

            ApiKey apiKey = ApiKey.builder()
                    .name(utils.getRandomElement(List.of(keyNames)) + " #" + (i + 1))
                    .keyHash(keyHash)
                    .keyPrefix(keyPrefix)
                    .rateLimitPerHour(ThreadLocalRandom.current().nextInt(100, 5000))
                    .active(utils.getRandom().nextDouble() < 0.9)
                    .lastUsedAt(utils.getRandom().nextDouble() < 0.7 ?
                            utils.getRandomInstantBetween(2024, 2024) : null)
                    .expiresAt(utils.getRandom().nextDouble() < 0.3 ?
                            createdAt.plus(ThreadLocalRandom.current().nextLong(90, 365), ChronoUnit.DAYS) : null)
                    .createdAt(createdAt)
                    .updatedAt(utils.getRandomInstantAfter(createdAt))
                    .user(randomUser)
                    .build();

            apiKeys.add(apiKey);
        }

        apiKeyRepository.saveAll(apiKeys);
    }
}
