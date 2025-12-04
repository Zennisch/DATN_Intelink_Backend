package intelink.modules.url.services;

import intelink.dto.url.CreateShortUrlRequest;
import intelink.models.ShortUrl;
import intelink.models.ShortUrlAccessControl;
import intelink.models.User;
import intelink.models.enums.AccessControlMode;
import intelink.models.enums.AccessControlType;
import intelink.modules.auth.services.AuthService;
import intelink.modules.url.repositories.ShortUrlRepository;
import intelink.utils.AccessControlValidationUtil;
import intelink.utils.FPEGenerator;
import intelink.utils.helper.Cipher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final AuthService authService;
    private final ShortUrlAccessControlService shortUrlAccessControlService;
    private final PasswordEncoder passwordEncoder;
    private final FPEGenerator fpeGenerator;

    private final Integer SHORT_CODE_LENGTH = 10;
    private final Integer MAX_SHORT_CODE_GENERATION_ATTEMPTS = 20;

    @Transactional
    public ShortUrl createShortUrl(User user, CreateShortUrlRequest request) throws IllegalBlockSizeException, BadPaddingException {
        // 1. Calculate expiry date (7 days default)
        Instant expiresAt = request.availableDays() != null
                ? Instant.now().plusSeconds(request.availableDays() * 24 * 60 * 60)
                : Instant.now().plusSeconds(7 * 24 * 60 * 60);

        // 2. Determine short code
        String shortCode = null;
        byte[] shortCodeTweak = null;
        ShortUrl shortUrl = null;

        if (request.customCode() != null && !request.customCode().isEmpty()) {
            String customCode = request.customCode();
            if (!StringUtils.hasText(customCode)) {
                throw new IllegalArgumentException("Custom code cannot be empty");
            }
            if (customCode.length() < 4 || customCode.length() > 16) {
                throw new IllegalArgumentException("Custom code must be between 4 and 16 characters long");
            }
            if (!customCode.matches("^[a-zA-Z0-9_-]+$")) {
                throw new IllegalArgumentException("Custom code can only contain letters, numbers, hyphens, and underscores");
            }
            if (shortUrlRepository.existsByShortCode(customCode)) {
                throw new IllegalArgumentException("Custom code already exists");
            }

            shortCode = customCode;
            shortUrl = ShortUrl.builder()
                    .originalUrl(request.originalUrl())
                    .user(user)
                    .build();
        } else {
            for (int attempts = 0; attempts < MAX_SHORT_CODE_GENERATION_ATTEMPTS; attempts++) {
                shortUrl = ShortUrl.builder()
                        .originalUrl(request.originalUrl())
                        .user(user)
                        .build();
                shortUrlRepository.save(shortUrl); // Save to get an ID

                Cipher cipher = fpeGenerator.generate(shortUrl.getId(), SHORT_CODE_LENGTH);
                if (!shortUrlRepository.existsByShortCode(cipher.text())) {
                    shortCode = cipher.text();
                    shortCodeTweak = cipher.tweak();
                    break;
                } else {
                    shortUrlRepository.delete(shortUrl); // Delete if collision occurs
                }
            }
        }

        // 3. Validate short code uniqueness
        if (shortCode == null) {
            throw new IllegalStateException("Failed to generate a unique short code after " + MAX_SHORT_CODE_GENERATION_ATTEMPTS + " attempts");
        }

        // 4. Create and save short URL
        shortUrl.setShortCode(shortCode);
        shortUrl.setShortCodeTweak(shortCodeTweak);
        shortUrl.setTitle(request.title());
        shortUrl.setDescription(request.description());
        shortUrl.setEnabled(true);
        shortUrl.setMaxUsage(request.maxUsage());
        shortUrl.setExpiresAt(expiresAt);
        shortUrlRepository.save(shortUrl);

        // 5. Encode password if provided
        if (request.password() != null && !request.password().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(request.password());
            ShortUrlAccessControl passwordAccessControl = ShortUrlAccessControl.builder()
                    .shortUrl(shortUrl)
                    .type(AccessControlType.PASSWORD_PROTECTED)
                    .value(encodedPassword)
                    .build();
            shortUrlAccessControlService.save(passwordAccessControl);
        }

        // 6. Set other access control rules
        if (request.accessControlMode() != null && request.accessControlMode() != AccessControlMode.NONE) {
            if (request.accessControlCIDRs() != null) {
                for (String cidr : request.accessControlCIDRs()) {
                    AccessControlValidationUtil.validateCIDR(cidr);
                    ShortUrlAccessControl cidrAccessControl = ShortUrlAccessControl.builder()
                            .shortUrl(shortUrl)
                            .type(AccessControlType.CIDR)
                            .value(cidr)
                            .build();
                    shortUrlAccessControlService.save(cidrAccessControl);
                }
            }
            if (request.accessControlGeographies() != null) {
                for (String geography : request.accessControlGeographies()) {
                    AccessControlValidationUtil.validateGeography(geography);
                    ShortUrlAccessControl geoAccessControl = ShortUrlAccessControl.builder()
                            .shortUrl(shortUrl)
                            .type(AccessControlType.GEOGRAPHY)
                            .value(geography)
                            .build();
                    shortUrlAccessControlService.save(geoAccessControl);
                }
            }
            shortUrl.setAccessControlMode(request.accessControlMode());
        }

        // 7. Save the short URL
        shortUrlRepository.save(shortUrl);

        // 8. Update user's total short URLs count
        authService.increaseTotalShortUrls(user.getId());
        log.info("[ShortUrlService.create] Short URL created with code: {}", shortCode);

        return shortUrl;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "shortUrls", key = "#shortCode")
    public Optional<ShortUrl> findByShortCode(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode);
    }

    @Transactional
    public void incrementAllowedCounters(Long shortUrlId, Integer uniqueIncrement) {
        shortUrlRepository.increaseAllowedCounters(shortUrlId, uniqueIncrement);
    }

    @Transactional
    public void incrementBlockedCounters(Long shortUrlId) {
        shortUrlRepository.increaseBlockedCounters(shortUrlId);
    }

    @Transactional
    public Page<ShortUrl> getShortUrlsByUser(User user, Pageable pageable) {
        return null;
    }
}
