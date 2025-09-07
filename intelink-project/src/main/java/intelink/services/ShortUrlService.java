package intelink.services;

import intelink.dto.object.Cipher;
import intelink.dto.request.url.CreateShortUrlRequest;
import intelink.dto.response.url.UnlockUrlResponse;
import intelink.dto.response.analysis.ThreatAnalysisResult;
import intelink.dto.response.analysis.ThreatMatchInfo;
import intelink.models.ShortUrl;
import intelink.models.ShortUrlAnalysisResult;
import intelink.models.User;
import intelink.models.enums.ShortUrlAnalysisEngine;
import intelink.models.enums.ShortUrlAnalysisStatus;
import intelink.models.enums.ShortUrlStatus;
import intelink.repositories.ShortUrlRepository;
import intelink.services.interfaces.IClickLogService;
import intelink.services.interfaces.IShortUrlService;
import intelink.services.interfaces.IUserService;
import intelink.utils.FPEUtil;
import intelink.utils.GoogleSafeBrowsingUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortUrlService implements IShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final IUserService userService;
    private final IClickLogService clickLogService;
    private final PasswordEncoder passwordEncoder;
    private final GoogleSafeBrowsingUtil googleSafeBrowsingUtil;
    private final AnalysisResultService analysisResultService;

    private final Integer SHORT_CODE_LENGTH = 10;

    public User getCurrentUser(UserDetails userDetails) {
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        return userOpt.get();
    }

    private ShortUrl findUserShortUrl(Long userId, String shortCode) {
        Optional<ShortUrl> shortUrlOpt = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrlOpt.isEmpty() || !shortUrlOpt.get().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Short URL not found or does not belong to the user");
        }
        return shortUrlOpt.get();
    }

    @Transactional
    public ShortUrl create(User user, CreateShortUrlRequest request) throws IllegalBlockSizeException, BadPaddingException {
        // 1. Generate short code
        Cipher cipher = FPEUtil.generate(user.getId(), SHORT_CODE_LENGTH);
        String shortCode = cipher.getText();

        // 2. Calculate expiry date
        Instant expiresAt = Instant.now().plusSeconds(request.getAvailableDays() * 24 * 60 * 60);

        // 3. Encode password if provided
        String encodedPassword = null;
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        // 4. Create and save short URL
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .originalUrl(request.getOriginalUrl())
                .passwordHash(encodedPassword)
                .description(request.getDescription())
                .status(ShortUrlStatus.ENABLED)
                .maxUsage(request.getMaxUsage())
                .expiresAt(expiresAt)
                .user(user)
                .build();

        ShortUrl savedUrl = shortUrlRepository.save(shortUrl);
        userService.increaseTotalShortUrls(user.getId());
        log.info("ShortUrlService.create: Short URL created with code: {}", shortCode);

        // 5. Perform threat analysis
        performThreatAnalysis(savedUrl);

        return savedUrl;
    }

    /**
     * Perform threat analysis on the URL
     */
    private void performThreatAnalysis(ShortUrl shortUrl) {
        try {
            ThreatAnalysisResult threatAnalysisResult = googleSafeBrowsingUtil.checkUrls(List.of(shortUrl.getOriginalUrl()));
            
            if (!threatAnalysisResult.hasMatches() || threatAnalysisResult.matches().isEmpty()) {
                // URL is safe
                ShortUrlAnalysisResult analysisResult = ShortUrlAnalysisResult.builder()
                        .shortUrl(shortUrl)
                        .status(ShortUrlAnalysisStatus.SAFE)
                        .engine(ShortUrlAnalysisEngine.GOOGLE_SAFE_BROWSING)
                        .threatType("NONE")
                        .platformType("ANY_PLATFORM")
                        .build();
                analysisResultService.save(analysisResult);
                log.info("ShortUrlService.performThreatAnalysis: URL {} is safe", shortUrl.getShortCode());
            } else {
                // URL has threats - create analysis records and delete URL
                for (ThreatMatchInfo match : threatAnalysisResult.matches()) {
                    ShortUrlAnalysisResult analysisResult = ShortUrlAnalysisResult.builder()
                            .shortUrl(shortUrl)
                            .status(ShortUrlAnalysisStatus.fromString(match.threatType()))
                            .engine(ShortUrlAnalysisEngine.GOOGLE_SAFE_BROWSING)
                            .threatType(match.threatType())
                            .platformType(match.platformType())
                            .cacheDuration(match.cacheDuration())
                            .details("Threat detected: " + match.threatEntryType())
                            .createdAt(Instant.now())
                            .build();
                    analysisResultService.save(analysisResult);
                }
                deleteShortUrl(shortUrl.getUser().getId(), shortUrl.getShortCode());
                log.warn("ShortUrlService.performThreatAnalysis: URL {} marked as threat and deleted", shortUrl.getShortCode());
            }
        } catch (Exception e) {
            log.error("ShortUrlService.performThreatAnalysis: Error analyzing URL {}: {}", shortUrl.getShortCode(), e.getMessage());
        }
    }

    @Transactional
    public void enableShortUrl(Long userId, String shortCode) {
        ShortUrl shortUrl = findUserShortUrl(userId, shortCode);
        shortUrl.setStatus(ShortUrlStatus.ENABLED);
        shortUrlRepository.save(shortUrl);
        log.info("ShortUrlService.enableShortUrl: URL {} enabled", shortCode);
    }

    @Transactional
    public void disableShortUrl(Long userId, String shortCode) {
        ShortUrl shortUrl = findUserShortUrl(userId, shortCode);
        shortUrl.setStatus(ShortUrlStatus.DISABLED);
        shortUrlRepository.save(shortUrl);
        log.info("ShortUrlService.disableShortUrl: URL {} disabled", shortCode);
    }

    @Transactional
    public void deleteShortUrl(Long userId, String shortCode) {
        ShortUrl shortUrl = findUserShortUrl(userId, shortCode);
        shortUrl.setDeletedAt(Instant.now());
        shortUrlRepository.save(shortUrl);
        userService.decreaseTotalShortUrls(userId);
        log.info("ShortUrlService.deleteShortUrl: URL {} soft deleted", shortCode);
    }

    @Transactional(readOnly = true)
    public Optional<ShortUrl> findByShortCode(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode);
    }

    @Transactional(readOnly = true)
    public Optional<ShortUrl> findByShortCodeAndUserId(String shortCode, Long userId) {
        Optional<ShortUrl> shortUrlOpt = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrlOpt.isPresent() && shortUrlOpt.get().getUser().getId().equals(userId)) {
            return shortUrlOpt;
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Page<ShortUrl> getUserShortUrls(Long userId, Pageable pageable) {
        return shortUrlRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ShortUrl> getUserShortUrlsWithSorting(Long userId, Pageable pageable) {
        return shortUrlRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public ShortUrl updateShortUrl(Long userId, String shortCode, String description, Long maxUsage, Integer availableDays) {
        ShortUrl shortUrl = findUserShortUrl(userId, shortCode);
        
        if (description != null) {
            shortUrl.setDescription(description);
        }
        
        if (maxUsage != null) {
            shortUrl.setMaxUsage(maxUsage);
        }
        
        if (availableDays != null) {
            Instant newExpiresAt = Instant.now().plusSeconds(availableDays * 24 * 60 * 60);
            shortUrl.setExpiresAt(newExpiresAt);
        }
        
        ShortUrl savedUrl = shortUrlRepository.save(shortUrl);
        log.info("ShortUrlService.updateShortUrl: URL {} updated", shortCode);
        return savedUrl;
    }

    @Transactional
    public ShortUrl updatePassword(Long userId, String shortCode, String newPassword, String currentPassword) {
        ShortUrl shortUrl = findUserShortUrl(userId, shortCode);
        
        // Verify current password if URL already has a password
        if (shortUrl.getPasswordHash() != null) {
            if (currentPassword == null || !passwordEncoder.matches(currentPassword, shortUrl.getPasswordHash())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
        }
        
        // Set new password
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            shortUrl.setPasswordHash(passwordEncoder.encode(newPassword));
        } else {
            shortUrl.setPasswordHash(null); // Remove password
        }
        
        ShortUrl savedUrl = shortUrlRepository.save(shortUrl);
        log.info("ShortUrlService.updatePassword: Password updated for URL {}", shortCode);
        return savedUrl;
    }

    @Transactional
    public void increaseTotalClicks(String shortCode) {
        Optional<ShortUrl> shortUrlOpt = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrlOpt.isPresent()) {
            ShortUrl shortUrl = shortUrlOpt.get();
            shortUrl.setTotalClicks(shortUrl.getTotalClicks() + 1);
            shortUrlRepository.save(shortUrl);
            
            // Also increase user's total clicks
            userService.increaseTotalClicks(shortUrl.getUser().getId());
            
            log.debug("ShortUrlService.increaseTotalClicks: Incremented clicks for URL {} to {}", 
                     shortCode, shortUrl.getTotalClicks());
        } else {
            log.warn("ShortUrlService.increaseTotalClicks: Short URL not found: {}", shortCode);
        }
    }

    @Transactional(readOnly = true)
    public Boolean isUrlAccessible(ShortUrl shortUrl, String password) {
        // 1. Check if URL is deleted
        if (shortUrl.getDeletedAt() != null) {
            log.debug("ShortUrlService.isUrlAccessible: URL {} is deleted", shortUrl.getShortCode());
            return false;
        }

        // 2. Check if URL is disabled
        if (shortUrl.getStatus() == ShortUrlStatus.DISABLED) {
            log.debug("ShortUrlService.isUrlAccessible: URL {} is disabled", shortUrl.getShortCode());
            return false;
        }

        // 3. Check if URL has expired
        if (shortUrl.getExpiresAt() != null && Instant.now().isAfter(shortUrl.getExpiresAt())) {
            log.debug("ShortUrlService.isUrlAccessible: URL {} has expired", shortUrl.getShortCode());
            return false;
        }

        // 4. Check if URL has reached max usage
        if (shortUrl.getMaxUsage() != null && shortUrl.getTotalClicks() >= shortUrl.getMaxUsage()) {
            log.debug("ShortUrlService.isUrlAccessible: URL {} has reached max usage", shortUrl.getShortCode());
            return false;
        }

        // 5. Check password if required
        if (shortUrl.getPasswordHash() != null) {
            boolean passwordValid = password != null && passwordEncoder.matches(password, shortUrl.getPasswordHash());
            log.debug("ShortUrlService.isUrlAccessible: Password validation for URL {}: {}", shortUrl.getShortCode(), passwordValid);
            return passwordValid;
        }

        return true;
    }

    @Transactional(readOnly = true)
    public UnlockUrlResponse getUnlockInfo(String shortCode) {
        Optional<ShortUrl> shortUrlOpt = findByShortCode(shortCode);
        
        if (shortUrlOpt.isEmpty()) {
            log.warn("ShortUrlService.getUnlockInfo: Short URL not found: {}", shortCode);
            throw new IllegalArgumentException("Short URL not found");
        }

        ShortUrl shortUrl = shortUrlOpt.get();
        
        // Check if URL requires password
        if (shortUrl.getPasswordHash() == null) {
            log.warn("ShortUrlService.getUnlockInfo: URL does not require password: {}", shortCode);
            throw new IllegalArgumentException("This URL does not require a password");
        }

        return UnlockUrlResponse.builder()
                .success(true)
                .message("Password required for this URL")
                .shortCode(shortCode)
                .build();
    }

    @Transactional
    public UnlockUrlResponse unlockUrl(String shortCode, String password, HttpServletRequest request) {
        // 1. Find short URL
        Optional<ShortUrl> shortUrlOpt = findByShortCode(shortCode);
        if (shortUrlOpt.isEmpty()) {
            log.warn("ShortUrlService.unlockUrl: Short URL not found: {}", shortCode);
            throw new IllegalArgumentException("Short URL not found");
        }

        // 2. Check if URL is accessible
        ShortUrl shortUrl = shortUrlOpt.get();
        boolean isAccessible = isUrlAccessible(shortUrl, password);
        
        if (!isAccessible) {
            log.warn("ShortUrlService.unlockUrl: Failed to unlock URL - incorrect password or URL unavailable: {}", shortCode);
            throw new IllegalArgumentException("Incorrect password or URL is unavailable");
        }

        // 3. Record click log
        clickLogService.record(shortCode, request);
        log.info("ShortUrlService.unlockUrl: URL unlocked successfully: {}", shortCode);
        
        return UnlockUrlResponse.success(shortUrl.getOriginalUrl(), shortCode);
    }
}