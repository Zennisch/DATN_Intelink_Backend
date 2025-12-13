package intelink.modules.url.services;

import intelink.dto.url.CreateShortUrlRequest;
import intelink.models.ShortUrl;
import intelink.models.ShortUrlAccessControl;
import intelink.models.ShortUrlAnalysisResult;
import intelink.models.Subscription;
import intelink.models.SubscriptionPlan;
import intelink.models.User;
import intelink.models.enums.AccessControlMode;
import intelink.models.enums.AccessControlType;
import intelink.modules.auth.services.AuthService;
import intelink.modules.subscription.services.SubscriptionService;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final AuthService authService;
    private final SubscriptionService subscriptionService;
    private final ShortUrlAccessControlService shortUrlAccessControlService;
    private final ShortUrlAnalysisService shortUrlAnalysisService;
    private final PasswordEncoder passwordEncoder;
    private final FPEGenerator fpeGenerator;

    private final Integer SHORT_CODE_LENGTH = 10;
    private final Integer GUEST_MAX_URL_EXPIRY_DAYS = 1; // Guest URLs expire in 1 day
    private final Integer GUEST_MAX_USAGE_PER_URL = 10; // Guest URLs limited to 10 clicks

    /**
     * Validate guest user limitations (stricter than subscription limits)
     */
    private void validateGuestLimits(CreateShortUrlRequest request) {
        // 1. No custom codes allowed for guests
        if (request.customCode() != null && !request.customCode().isEmpty()) {
            throw new IllegalArgumentException(
                    "Custom short codes are not available for guest users. Please sign up to use this feature.");
        }
        
        // 2. No password protection for guests
        if (request.password() != null && !request.password().isEmpty()) {
            throw new IllegalArgumentException(
                    "Password protection is not available for guest users. Please sign up to use this feature.");
        }
        
        // 3. No access control for guests
        if (request.accessControlMode() != null && request.accessControlMode() != AccessControlMode.NONE) {
            throw new IllegalArgumentException(
                    "Access control is not available for guest users. Please sign up to use this feature.");
        }
        
        if ((request.accessControlCIDRs() != null && !request.accessControlCIDRs().isEmpty()) ||
            (request.accessControlGeographies() != null && !request.accessControlGeographies().isEmpty())) {
            throw new IllegalArgumentException(
                    "Access control is not available for guest users. Please sign up to use this feature.");
        }
        
        // 4. Limit expiry time to 1 day max
        if (request.availableDays() != null && request.availableDays() > GUEST_MAX_URL_EXPIRY_DAYS) {
            throw new IllegalArgumentException(
                    String.format("Guest users can only create short URLs valid for up to %d day(s). Please sign up for longer validity.",
                            GUEST_MAX_URL_EXPIRY_DAYS));
        }
        
        // 5. Limit max usage
        if (request.maxUsage() != null && request.maxUsage() > GUEST_MAX_USAGE_PER_URL) {
            throw new IllegalArgumentException(
                    String.format("Guest users can set maximum usage up to %d clicks. Please sign up for higher limits.",
                            GUEST_MAX_USAGE_PER_URL));
        }
        
        log.info("[ShortUrlService.validateGuestLimits] Guest user validation passed");
    }

    /**
     * Validate user's subscription limits before creating short URL
     */
    private void validateSubscriptionLimits(User user, CreateShortUrlRequest request) {
        // Get active subscription
        Subscription subscription = subscriptionService.getActiveSubscriptionByUser(user)
                .orElseThrow(() -> new IllegalStateException("No active subscription found. Please subscribe to a plan."));
        
        SubscriptionPlan plan = subscription.getSubscriptionPlan();
        
        // 1. Check total short URLs limit
        long currentShortUrlCount = shortUrlRepository.countByUserAndDeletedAtIsNull(user);
        if (currentShortUrlCount >= plan.getMaxShortUrls()) {
            throw new IllegalArgumentException(
                    String.format("You have reached the maximum number of short URLs (%d) for your %s plan. Please upgrade to create more.",
                            plan.getMaxShortUrls(), plan.getType().name()));
        }
        
        // 2. Check custom code permission
        if (request.customCode() != null && !request.customCode().isEmpty()) {
            if (!plan.getShortCodeCustomizationEnabled()) {
                throw new IllegalArgumentException(
                        String.format("Custom short codes are not available in your %s plan. Please upgrade to use this feature.",
                                plan.getType().name()));
            }
        }
        
        // 3. Validate maxUsage against plan limit
        if (request.maxUsage() != null) {
            if (request.maxUsage() > plan.getMaxUsagePerUrl()) {
                throw new IllegalArgumentException(
                        String.format("Maximum usage per URL cannot exceed %d for your %s plan. Requested: %d",
                                plan.getMaxUsagePerUrl(), plan.getType().name(), request.maxUsage()));
            }
        }
        
        log.info("[ShortUrlService.validateSubscriptionLimits] User {} validated. Current: {}/{} URLs, Plan: {}",
                user.getId(), currentShortUrlCount, plan.getMaxShortUrls(), plan.getType().name());
    }

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
     * Supports both authenticated users (with subscription) and guest users (with strict limits)
     */
    public ShortUrl createShortUrl(User user, CreateShortUrlRequest request) throws IllegalBlockSizeException, BadPaddingException {
        // 0. Validate based on user type
        if (user != null) {
            // Authenticated user - validate subscription limits
            validateSubscriptionLimits(user, request);
        } else {
            // Guest user - validate guest limits (stricter)
            validateGuestLimits(request);
        }
        
        // 1. Calculate expiry date
        Instant expiresAt;
        if (user != null) {
            // Authenticated: 7 days default or user-specified
            expiresAt = request.availableDays() != null
                    ? Instant.now().plusSeconds(request.availableDays() * 24 * 60 * 60)
                    : Instant.now().plusSeconds(7 * 24 * 60 * 60);
        } else {
            // Guest: 1 day max
            int guestDays = (request.availableDays() != null && request.availableDays() <= GUEST_MAX_URL_EXPIRY_DAYS)
                    ? request.availableDays()
                    : GUEST_MAX_URL_EXPIRY_DAYS;
            expiresAt = Instant.now().plusSeconds(guestDays * 24 * 60 * 60);
        }

        // 2. Transaction 1: Create and save the base ShortUrl entity
        ShortUrl shortUrl = createBaseShortUrl(user, request, expiresAt);

        // 3. Transaction 2: Save password access control (if provided and user is authenticated)
        if (user != null && request.password() != null && !request.password().isEmpty()) {
            savePasswordAccessControl(shortUrl, request.password());
        }

        // 4. Transaction 3: Save CIDR and Geography access controls (if provided and user is authenticated)
        if (user != null && request.accessControlMode() != null && request.accessControlMode() != AccessControlMode.NONE) {
            saveAccessControls(shortUrl, request);
            updateAccessControlMode(shortUrl.getId(), request.accessControlMode());
        }

        // 5. Transaction 4: Update user's total short URLs count (only for authenticated users)
        if (user != null) {
            authService.increaseTotalShortUrls(user.getId());
        }

        // 6. Async: Analyze URL for security threats (runs independently)
        shortUrlAnalysisService.analyzeUrl(shortUrl);

        log.info("[ShortUrlService.create] Short URL created with code: {} (User: {})", 
                shortUrl.getShortCode(), user != null ? user.getId() : "GUEST");
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
        return shortUrlRepository.findByUserAndDeletedAtIsNull(user, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ShortUrl> searchShortUrls(User user, String query, String status, Pageable pageable) {
        Boolean enabled = null;
        if (status != null && !status.isEmpty()) {
            if ("active".equalsIgnoreCase(status) || "true".equalsIgnoreCase(status) || "enabled".equalsIgnoreCase(status)) {
                enabled = true;
            } else if ("inactive".equalsIgnoreCase(status) || "false".equalsIgnoreCase(status) || "disabled".equalsIgnoreCase(status)) {
                enabled = false;
            }
        }

        String searchQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        return shortUrlRepository.searchWithFilters(user, searchQuery, enabled, pageable);
    }

    @Transactional(readOnly = true)
    public ShortUrl getShortUrlByShortCode(User user, String shortCode) {
        return shortUrlRepository.findByShortCodeAndUser(shortCode, user)
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found"));
    }

    @Transactional
    public ShortUrl updateShortUrl(User user, String shortCode, CreateShortUrlRequest request) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCodeAndUser(shortCode, user)
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found"));

        // Update basic fields
        if (request.originalUrl() != null) {
            shortUrl.setOriginalUrl(request.originalUrl());
        }
        if (request.title() != null) {
            shortUrl.setTitle(request.title());
        }
        if (request.description() != null) {
            shortUrl.setDescription(request.description());
        }
        if (request.maxUsage() != null) {
            shortUrl.setMaxUsage(request.maxUsage());
        }
        if (request.availableDays() != null) {
            Instant expiresAt = Instant.now().plusSeconds(request.availableDays() * 24 * 60 * 60);
            shortUrl.setExpiresAt(expiresAt);
        }

        // Update access control mode
        if (request.accessControlMode() != null) {
            shortUrl.setAccessControlMode(request.accessControlMode());
        }

        // Delete existing access controls
        shortUrlAccessControlService.deleteByShortUrl(shortUrl);

        // Save new password if provided
        if (request.password() != null && !request.password().isEmpty()) {
            savePasswordAccessControl(shortUrl, request.password());
        }

        // Save new access controls if provided
        if (request.accessControlMode() != null && request.accessControlMode() != AccessControlMode.NONE) {
            saveAccessControls(shortUrl, request);
        }

        return shortUrlRepository.save(shortUrl);
    }

    @Transactional
    public void deleteShortUrl(User user, String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCodeAndUser(shortCode, user)
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found"));
        shortUrl.setDeletedAt(Instant.now());
        authService.decreaseTotalShortUrls(user.getId());
        shortUrlRepository.save(shortUrl);
    }

    @Transactional
    public ShortUrl enableShortUrl(User user, String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCodeAndUser(shortCode, user)
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found"));
        shortUrl.setEnabled(true);
        return shortUrlRepository.save(shortUrl);
    }

    @Transactional
    public ShortUrl disableShortUrl(User user, String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCodeAndUser(shortCode, user)
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found"));
        shortUrl.setEnabled(false);
        return shortUrlRepository.save(shortUrl);
    }

    @Transactional(readOnly = true)
    public List<ShortUrlAnalysisResult> getAnalysisResults(ShortUrl shortUrl) {
        return shortUrlAnalysisService.getAnalysisResults(shortUrl);
    }
}
