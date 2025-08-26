package intelink.utils.dataseeding;

import intelink.models.User;
import intelink.models.enums.OAuthProvider;
import intelink.models.enums.UserRole;
import intelink.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDataSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataSeedingUtils utils;

    public List<User> createUsers(int count) {
        log.info("Creating {} users...", count);
        List<User> users = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            User user = User.builder()
                    .username("user" + String.format("%03d", i))
                    .email("user" + i + "@example.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(i <= 5 ? UserRole.ADMIN : UserRole.USER)
                    .totalClicks(ThreadLocalRandom.current().nextLong(0, 1000))
                    .totalShortUrls(ThreadLocalRandom.current().nextInt(0, 50))
                    .emailVerified(utils.getRandom().nextBoolean())
                    .authProvider(utils.getRandom().nextDouble() < 0.7 ? OAuthProvider.LOCAL : utils.getRandomOAuthProvider())
                    .providerUserId(utils.getRandom().nextDouble() < 0.3 ? "provider_" + UUID.randomUUID().toString().substring(0, 8) : null)
                    .lastLoginAt(utils.getRandomInstantBetween(2023, 2024))
                    .createdAt(utils.getRandomInstantBetween(2023, 2023))
                    .updatedAt(Instant.now())
                    .build();

            users.add(user);
        }

        return userRepository.saveAll(users);
    }
}
