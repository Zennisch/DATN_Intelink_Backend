package intelink.utils.dataseeding;

import intelink.models.OAuthAccount;
import intelink.models.User;
import intelink.models.VerificationToken;
import intelink.models.enums.OAuthProvider;
import intelink.repositories.OAuthAccountRepository;
import intelink.repositories.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityDataSeeder {

    private final VerificationTokenRepository verificationTokenRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final DataSeedingUtils utils;

    public void createVerificationTokens(List<User> users, int count) {
        log.info("Creating {} verification tokens...", count);
        List<VerificationToken> tokens = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            User randomUser = utils.getRandomElement(users);
            Instant createdAt = utils.getRandomInstantBetween(2023, 2024);

            VerificationToken token = VerificationToken.builder()
                    .token(UUID.randomUUID().toString())
                    .type(utils.getRandomTokenType())
                    .used(utils.getRandom().nextBoolean())
                    .expiresAt(createdAt.plus(24, ChronoUnit.HOURS))
                    .createdAt(createdAt)
                    .user(randomUser)
                    .build();

            tokens.add(token);
        }

        verificationTokenRepository.saveAll(tokens);
    }

    public void createOAuthAccounts(List<User> users, int count) {
        log.info("Creating {} OAuth accounts...", count);
        List<OAuthAccount> accounts = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            User randomUser = utils.getRandomElement(users);
            OAuthProvider provider = utils.getRandomOAuthProvider();
            Instant createdAt = utils.getRandomInstantBetween(2023, 2024);

            OAuthAccount account = OAuthAccount.builder()
                    .provider(provider)
                    .providerUserId("oauth_" + provider.name().toLowerCase() + "_" + i)
                    .providerUsername("oauth_user_" + i)
                    .providerEmail("oauth" + i + "@" + provider.name().toLowerCase() + ".com")
                    .accessToken("access_token_" + UUID.randomUUID())
                    .refreshToken(utils.getRandom().nextDouble() < 0.7 ? "refresh_token_" + UUID.randomUUID() : null)
                    .tokenExpiresAt(createdAt.plus(30, ChronoUnit.DAYS))
                    .createdAt(createdAt)
                    .updatedAt(utils.getRandomInstantAfter(createdAt))
                    .user(randomUser)
                    .build();

            accounts.add(account);
        }

        oAuthAccountRepository.saveAll(accounts);
    }
}
