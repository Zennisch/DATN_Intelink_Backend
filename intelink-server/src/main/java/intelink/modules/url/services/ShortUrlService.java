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

    @Transactional
    public String validateCustomCode(String customCode) {
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
        return customCode;
    }

    /**
     * Main orchestration method - NO @Transactional to allow independent transactions
     * This reduces deadlock risk by minimizing lock duration and separating concerns
     */
    public ShortUrl createShortUrl(User user, CreateShortUrlRequest request) throws IllegalBlockSizeException, BadPaddingException {
        // 1. Calculate expiry date (7 days default)
        Instant expiresAt = request.availableDays() != null
                ? Instant.now().plusSeconds(request.availableDays() * 24 * 60 * 60)
                : Instant.now().plusSeconds(7 * 24 * 60 * 60);

        // 2. Transaction 1: Create and save the base ShortUrl entity
        ShortUrl shortUrl = createBaseShortUrl(user, request, expiresAt);

        // 3. Transaction 2: Save password access control (if provided)
        if (request.password() != null && !request.password().isEmpty()) {
            savePasswordAccessControl(shortUrl, request.password());
        }

        // 4. Transaction 3: Save CIDR and Geography access controls (if provided)
        if (request.accessControlMode() != null && request.accessControlMode() != AccessControlMode.NONE) {
            saveAccessControls(shortUrl, request);
            updateAccessControlMode(shortUrl.getId(), request.accessControlMode());
        }

        // 5. Transaction 4: Update user's total short URLs count (separate transaction)
        authService.increaseTotalShortUrls(user.getId());

        log.info("[ShortUrlService.create] Short URL created with code: {}", shortUrl.getShortCode());
        return shortUrl;
    }

    /**
     * Transaction 1: Create and persist the base ShortUrl entity
     * Minimizes lock time by only handling ShortUrl table
     */
    @Transactional
    protected ShortUrl createBaseShortUrl(User user, CreateShortUrlRequest request, Instant expiresAt)
            throws IllegalBlockSizeException, BadPaddingException {
        String shortCode;
        byte[] shortCodeTweak = null;
        ShortUrl shortUrl;

        if (request.customCode() != null && !request.customCode().isEmpty()) {
            shortCode = validateCustomCode(request.customCode());
            shortUrl = ShortUrl.builder()
                    .originalUrl(request.originalUrl())
                    .user(user)
                    .shortCode(shortCode)
                    .title(request.title())
                    .description(request.description())
                    .enabled(true)
                    .maxUsage(request.maxUsage())
                    .expiresAt(expiresAt)
                    .build();
        } else {
            shortUrl = ShortUrl.builder()
                    .originalUrl(request.originalUrl())
                    .user(user)
                    .title(request.title())
                    .description(request.description())
                    .enabled(true)
                    .maxUsage(request.maxUsage())
                    .expiresAt(expiresAt)
                    .build();
            shortUrlRepository.save(shortUrl);

            // Generate short code using FPE
            Cipher cipher = fpeGenerator.generate(shortUrl.getId(), SHORT_CODE_LENGTH);
            shortCode = cipher.text();
            shortCodeTweak = cipher.tweak();

            if (shortCode == null) {
                throw new IllegalStateException("Failed to generate a unique short code");
            }

            shortUrl.setShortCode(shortCode);
            shortUrl.setShortCodeTweak(shortCodeTweak);
        }

        return shortUrlRepository.save(shortUrl);
    }

    @Transactional
    protected void savePasswordAccessControl(ShortUrl shortUrl, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        ShortUrlAccessControl passwordAccessControl = ShortUrlAccessControl.builder()
                .shortUrl(shortUrl)
                .type(AccessControlType.PASSWORD_PROTECTED)
                .value(encodedPassword)
                .build();
        shortUrlAccessControlService.save(passwordAccessControl);
    }

    @Transactional
    protected void saveAccessControls(ShortUrl shortUrl, CreateShortUrlRequest request) {
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
    }

    @Transactional
    protected void updateAccessControlMode(Long shortUrlId, AccessControlMode mode) {
        ShortUrl shortUrl = shortUrlRepository.findById(shortUrlId)
                .orElseThrow(() -> new IllegalStateException("ShortUrl not found"));
        shortUrl.setAccessControlMode(mode);
        shortUrlRepository.save(shortUrl);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "shortUrls", key = "#shortCode")
    public Optional<ShortUrl> findByShortCode(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode);
    }

    @Cacheable(value = "shortUrlsById", key = "#id")
    public Optional<ShortUrl> findById(Long id) {
        return shortUrlRepository.findById(id);
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
