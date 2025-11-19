package intelink.modules.url;

import intelink.dto.url.CreateShortUrlRequest;
import intelink.helper.Cipher;
import intelink.models.ShortUrl;
import intelink.models.ShortUrlAccessControl;
import intelink.models.User;
import intelink.models.enums.AccessControlMode;
import intelink.models.enums.AccessControlType;
import intelink.modules.user.UserService;
import intelink.utils.AccessControlValidationUtil;
import intelink.utils.FPEUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final UserService userService;
    private final ShortUrlAccessControlService shortUrlAccessControlService;
    private final PasswordEncoder passwordEncoder;

    private final Integer SHORT_CODE_LENGTH = 10;

    private String validateCustomCode(String customCode) {
        if (!StringUtils.hasText(customCode)) {
            throw new IllegalArgumentException("Custom code cannot be empty");
        }
        if (customCode.length() < 4 || customCode.length() > 16) {
            throw new IllegalArgumentException("Custom code must be between 4 and 20 characters");
        }
        if (!customCode.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("Custom code can only contain letters, numbers, hyphens, and underscores");
        }
        return customCode;
    }

    private String generateShortCode(Long userId) throws IllegalBlockSizeException, BadPaddingException {
        final int maxAttempts = 5;

        for (int attempts = 0; attempts < maxAttempts; attempts++) {
            Cipher cipher = FPEUtil.generate(userId, SHORT_CODE_LENGTH);
            String candidate = cipher.text();
            if (!shortUrlRepository.existsByShortCode(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Failed to generate a unique short code after " + maxAttempts + " attempts");
    }

    @Transactional
    public ShortUrl createShortUrl(User user, CreateShortUrlRequest request) throws IllegalBlockSizeException, BadPaddingException {
        // 1. Determine short code
        String shortCode;
        if (request.customCode() != null && !request.customCode().isEmpty()) {
            shortCode = validateCustomCode(request.customCode());
        } else {
            shortCode = generateShortCode(user.getId());
        }
        if (shortUrlRepository.existsByShortCode(shortCode)) {
            throw new IllegalArgumentException("Custom code already exists");
        }

        // 2. Calculate expiry date (7 days default)
        Instant expiresAt = request.availableDays() != null
                ? Instant.now().plusSeconds(request.availableDays() * 24 * 60 * 60)
                : Instant.now().plusSeconds(7 * 24 * 60 * 60);

        // 3. Create and save short URL
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .originalUrl(request.originalUrl())
                .title(request.title())
                .description(request.description())
                .enabled(true)
                .maxUsage(request.maxUsage())
                .expiresAt(expiresAt)
                .user(user)
                .build();
        shortUrlRepository.save(shortUrl);

        // 4. Encode password if provided
        if (request.password() != null && !request.password().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(request.password());
            ShortUrlAccessControl passwordAccessControl = ShortUrlAccessControl.builder()
                    .shortUrl(shortUrl)
                    .type(AccessControlType.PASSWORD_PROTECTED)
                    .value(encodedPassword)
                    .build();
            shortUrlAccessControlService.save(passwordAccessControl);
        }

        // 5. Set other access control rules
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

        // 6. Save the short URL
        shortUrlRepository.save(shortUrl);

        // 7. Update user's total short URLs count
        userService.increaseTotalShortUrls(user.getId());
        log.info("[ShortUrlService.create] Short URL created with code: {}", shortCode);

        return shortUrl;
    }

}
