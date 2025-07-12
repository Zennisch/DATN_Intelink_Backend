package intelink.services;

import intelink.models.ShortUrl;
import intelink.models.User;
import intelink.repositories.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortUrlService {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_CODE_LENGTH = 7;
    private static final SecureRandom random = new SecureRandom();
    private final ShortUrlRepository shortUrlRepository;
    private final UserService userService;

    @Transactional
    public ShortUrl createShortUrl(String originalUrl, User user, String description,
                                   Timestamp expiresAt, Long maxUsage, String password) {

        String shortCode = generateUniqueShortCode(user);

        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .originalUrl(originalUrl)
                .description(description)
                .expiresAt(expiresAt)
                .maxUsage(maxUsage)
                .password(password)
                .user(user)
                .isActive(true)
                .totalClicks(0L)
                .build();

        ShortUrl savedUrl = shortUrlRepository.save(shortUrl);
        userService.incrementUrlCount(user.getId());

        log.info("Created short URL: {} for user: {}", shortCode, user.getUsername());
        return savedUrl;
    }

    @Transactional(readOnly = true)
    public Optional<ShortUrl> findByShortCode(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode);
    }

    @Transactional
    public void incrementClickCount(String shortCode) {
        shortUrlRepository.incrementTotalClicks(shortCode);
        log.debug("Incremented click count for short code: {}", shortCode);
    }

    @Transactional(readOnly = true)
    public Page<ShortUrl> getUserUrls(Long userId, Pageable pageable) {
        return shortUrlRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<ShortUrl> getActiveUrlsByUser(Long userId) {
        return shortUrlRepository.findActiveByUserId(userId);
    }

    @Transactional
    public void deactivateExpiredUrls() {
        List<ShortUrl> expiredUrls = shortUrlRepository.findExpiredUrls(Timestamp.from(Instant.now()));
        if (!expiredUrls.isEmpty()) {
            List<Long> expiredIds = expiredUrls.stream().map(ShortUrl::getId).toList();
            shortUrlRepository.deactivateUrls(expiredIds);
            log.info("Deactivated {} expired URLs", expiredIds.size());
        }
    }

    @Transactional
    public void deactivateMaxUsageUrls() {
        List<ShortUrl> maxUsageUrls = shortUrlRepository.findMaxUsageReachedUrls();
        if (!maxUsageUrls.isEmpty()) {
            List<Long> maxUsageIds = maxUsageUrls.stream().map(ShortUrl::getId).toList();
            shortUrlRepository.deactivateUrls(maxUsageIds);
            log.info("Deactivated {} max usage reached URLs", maxUsageIds.size());
        }
    }

    @Transactional(readOnly = true)
    public boolean isUrlAccessible(ShortUrl shortUrl, String providedPassword) {
        if (!shortUrl.getIsActive()) {
            return false;
        }

        if (shortUrl.getExpiresAt() != null &&
                shortUrl.getExpiresAt().before(Timestamp.from(Instant.now()))) {
            return false;
        }

        if (shortUrl.getMaxUsage() != null &&
                shortUrl.getTotalClicks() >= shortUrl.getMaxUsage()) {
            return false;
        }

        return shortUrl.getPassword() == null ||
                shortUrl.getPassword().equals(providedPassword);
    }

    @Transactional(readOnly = true)
    public long countUserUrlsCreatedSince(Long userId, Instant since) {
        return shortUrlRepository.countByUserIdAndCreatedAtAfter(userId, since);
    }

    private String generateUniqueShortCode(User user) {
        String shortCode;
        int attempts = 0;

        do {
            // Generate a unique value based on user ID, timestamp, and random component
            long timestamp = System.currentTimeMillis();
            long userId = user.getId();
            int randomComponent = random.nextInt(1000000);

            // Create unique number combining these factors
            long uniqueNumber = timestamp ^ (userId << 16) ^ randomComponent;

            // Convert to Base62
            shortCode = encodeBase62(uniqueNumber);

            // Ensure correct length
            if (shortCode.length() < SHORT_CODE_LENGTH) {
                shortCode = padShortCode(shortCode);
            } else if (shortCode.length() > SHORT_CODE_LENGTH) {
                shortCode = shortCode.substring(0, SHORT_CODE_LENGTH);
            }

            attempts++;
            if (attempts > 10) {
                throw new RuntimeException("Unable to generate unique short code after 10 attempts");
            }
        } while (shortUrlRepository.findByShortCode(shortCode).isPresent());

        return shortCode;
    }

    private String encodeBase62(long number) {
        if (number == 0) {
            return String.valueOf(CHARACTERS.charAt(0));
        }

        StringBuilder sb = new StringBuilder();
        int base = CHARACTERS.length();

        while (number > 0) {
            int remainder = (int)(number % base);
            sb.insert(0, CHARACTERS.charAt(remainder));
            number /= base;
        }

        return sb.toString();
    }

    private String padShortCode(String shortCode) {
        StringBuilder sb = new StringBuilder(shortCode);
        while (sb.length() < SHORT_CODE_LENGTH) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH);
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    @Transactional
    public void deleteShortUrl(String shortCode, Long userId) {
        Optional<ShortUrl> shortUrl = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrl.isPresent() && shortUrl.get().getUser().getId().equals(userId)) {
            shortUrlRepository.delete(shortUrl.get());
            log.info("Deleted short URL: {} by user: {}", shortCode, userId);
        } else {
            throw new IllegalArgumentException("Short URL not found or access denied");
        }
    }
}