package intelink.utils.seeder;

import intelink.models.ShortUrl;
import intelink.models.User;
import intelink.repositories.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShortUrlDataSeeder {

    private final ShortUrlRepository shortUrlRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSeedingUtils utils;

    public List<ShortUrl> createShortUrls(List<User> users, int count) {
        log.info("Creating {} short URLs...", count);
        List<ShortUrl> shortUrls = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            User randomUser = utils.getRandomElement(users);
            Instant createdAt = utils.getRandomInstantBetween(2024, 2025);

            ShortUrl shortUrl = ShortUrl.builder()
                    .shortCode(utils.generateRandomShortCode())
                    .originalUrl("https://" + utils.getRandomElement(utils.domains) + "/page/" + i)
                    .passwordHash(utils.getRandom().nextDouble() < 0.2 ? passwordEncoder.encode("secret123") : null)
                    .description(utils.getRandom().nextDouble() < 0.5 ? "Description for URL " + i : null)
                    .maxUsage(utils.getRandom().nextDouble() < 0.3 ? ThreadLocalRandom.current().nextLong(10, 1000) : null)
                    .totalClicks(ThreadLocalRandom.current().nextLong(0, 500))
                    .expiresAt(createdAt.plus(ThreadLocalRandom.current().nextLong(30, 365), ChronoUnit.DAYS))
                    .createdAt(createdAt)
                    .updatedAt(utils.getRandomInstantAfter(createdAt))
                    .user(randomUser)
                    .build();

            shortUrls.add(shortUrl);
        }

        return shortUrlRepository.saveAll(shortUrls);
    }
}
