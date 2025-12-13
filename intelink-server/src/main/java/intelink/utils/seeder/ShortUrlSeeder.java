package intelink.utils.seeder;

import intelink.models.ShortUrl;
import intelink.models.User;
import intelink.models.enums.AccessControlMode;
import intelink.modules.auth.repositories.UserRepository;
import intelink.modules.url.repositories.ShortUrlRepository;
import intelink.utils.FPEGenerator;
import intelink.utils.helper.Cipher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class ShortUrlSeeder {

    private final ShortUrlRepository shortUrlRepository;
    private final UserRepository userRepository;
    private final FPEGenerator fpeGenerator;
    private final Integer SHORT_CODE_LENGTH = 10;

    @Transactional
    public void seed() {
        if (shortUrlRepository.count() == 0) {
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                return;
            }

            Random random = new Random();

            for (int i = 1; i <= 20; i++) {
                User randomUser = users.get(random.nextInt(users.size()));

                ShortUrl shortUrl = ShortUrl.builder()
                        .user(randomUser)
                        .title("Short URL " + i)
                        .description("Description for Short URL " + i)
                        .originalUrl("https://example.com/page-" + i)
                        .enabled(true)
                        .accessControlMode(AccessControlMode.NONE)
                        .totalClicks(0L)
                        .allowedClicks(0L)
                        .blockedClicks(0L)
                        .uniqueClicks(0L)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

                shortUrl = shortUrlRepository.save(shortUrl);

                try {
                    Cipher cipher = fpeGenerator.generate(shortUrl.getId(), SHORT_CODE_LENGTH);
                    shortUrl.setShortCode(cipher.text());
                    shortUrl.setShortCodeTweak(cipher.tweak());
                    shortUrlRepository.save(shortUrl);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to generate short code for seeder", e);
                }
            }
        }
    }
}
