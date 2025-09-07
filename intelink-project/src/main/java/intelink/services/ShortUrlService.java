package intelink.services;

import intelink.dto.helper.Cipher;
import intelink.dto.helper.threat.response.ThreatAnalysisResult;
import intelink.dto.helper.threat.response.ThreatMatchInfo;
import intelink.dto.request.CreateShortUrlRequest;
import intelink.models.AnalysisResult;
import intelink.models.ShortUrl;
import intelink.models.User;
import intelink.models.enums.AnalysisStatus;
import intelink.models.enums.ShortUrlStatus;
import intelink.repositories.ShortUrlRepository;
import intelink.services.interfaces.IShortUrlService;
import intelink.utils.FPEUtil;
import intelink.utils.GoogleSafeBrowsingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final GoogleSafeBrowsingUtil googleSafeBrowsingUtil;
    private final AnalysisResultService analysisResultService;

    private final Integer SHORT_CODE_LENGTH = 10;

    @Transactional
    public ShortUrl create(User user, CreateShortUrlRequest request) throws IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = FPEUtil.generate(user.getId(), SHORT_CODE_LENGTH);
        String shortCode = cipher.getText();
        Instant expiresAt = Instant.now().plusSeconds(request.getAvailableDays() * 24 * 60 * 60);

        String encodedPassword = null;
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .originalUrl(request.getOriginalUrl())
                .password(encodedPassword)
                .description(request.getDescription())
                .status(ShortUrlStatus.ENABLED)
                .maxUsage(request.getMaxUsage())
                .expiresAt(expiresAt)
                .user(user)
                .build();

        ShortUrl savedUrl = shortUrlRepository.save(shortUrl);
        userService.increaseTotalShortUrls(user.getId());

        ThreatAnalysisResult threatAnalysisResult = googleSafeBrowsingUtil.checkUrls(List.of(shortUrl.getOriginalUrl()));
        if (!threatAnalysisResult.hasMatches() || threatAnalysisResult.matches().isEmpty()) {
            AnalysisResult analysisResult = AnalysisResult.builder()
                    .shortUrl(shortUrl)
                    .status(AnalysisStatus.SAFE)
                    .analysisEngine("GOOGLE_SAFE_BROWSING")
                    .threatType("NONE")
                    .platformType("ANY_PLATFORM")
                    .build();
            analysisResultService.save(analysisResult);
        } else {
            for (ThreatMatchInfo match : threatAnalysisResult.matches()) {
                AnalysisResult analysisResult = AnalysisResult.builder()
                        .shortUrl(shortUrl)
                        .status(AnalysisStatus.fromString(match.threatType()))
                        .analysisEngine("GOOGLE_SAFE_BROWSING")
                        .threatType(match.threatType())
                        .platformType(match.platformType())
                        .cacheDuration(match.cacheDuration())
                        .details("Threat detected: " + match.threatEntryType())
                        .analyzedAt(Instant.now())
                        .build();
                analysisResultService.save(analysisResult);
            }
            deleteShortUrl(user.getId(), shortCode);
        }
        return savedUrl;
    }

    @Transactional
    public void enableShortUrl(Long userId, String shortCode) {
        Optional<ShortUrl> shortUrlOpt = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrlOpt.isEmpty() || !shortUrlOpt.get().getUser().getId().equals(userId)) {
            log.warn("ShortUrlService.enableShortUrl: Short URL with code {} not found for user ID {}", shortCode, userId);
            throw new IllegalArgumentException("Short URL not found or does not belong to the user");
        }
        ShortUrl shortUrl = shortUrlOpt.get();
        shortUrl.setStatus(ShortUrlStatus.ENABLED);
        shortUrlRepository.save(shortUrl);
    }

    @Transactional
    public void disableShortUrl(Long userId, String shortCode) {
        Optional<ShortUrl> shortUrlOpt = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrlOpt.isEmpty() || !shortUrlOpt.get().getUser().getId().equals(userId)) {
            log.warn("ShortUrlService.disableShortUrl: Short URL with code {} not found for user ID {}", shortCode, userId);
            throw new IllegalArgumentException("Short URL not found or does not belong to the user");
        }
        ShortUrl shortUrl = shortUrlOpt.get();
        shortUrl.setStatus(ShortUrlStatus.DISABLED);
        shortUrlRepository.save(shortUrl);
    }

    @Transactional
    public void deleteShortUrl(Long userId, String shortCode) {
        Optional<ShortUrl> shortUrlOpt = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrlOpt.isEmpty() || !shortUrlOpt.get().getUser().getId().equals(userId)) {
            log.warn("ShortUrlService.deleteShortUrl: Short URL with code {} not found for user ID {}", shortCode, userId);
            throw new IllegalArgumentException("Short URL not found or does not belong to the user");
        }
        ShortUrl shortUrl = shortUrlOpt.get();
        // shortUrl.setStatus(ShortUrlStatus.DELETED);
        shortUrlRepository.save(shortUrl);
        userService.decreaseTotalShortUrls(userId);
    }

    @Transactional(readOnly = true)
    public Optional<ShortUrl> findByShortCode(String shortCode) {
        log.debug("ShortUrlService.findByShortCode: Finding short URL with code: {}", shortCode);
        return shortUrlRepository.findByShortCode(shortCode);
    }

    @Transactional(readOnly = true)
    public Page<ShortUrl> getUserShortUrls(Long userId, Pageable pageable) {
        log.debug("ShortUrlService.getUserShortUrls: Fetching short URLs for user ID: {}", userId);
        return shortUrlRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ShortUrl> getUserShortUrlsWithSorting(Long userId, Pageable pageable) {
        log.debug("ShortUrlService.getUserShortUrlsWithSorting: Fetching short URLs for user ID: {} with custom sorting", userId);
        return shortUrlRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public void incrementTotalClicks(String shortCode) {
        log.debug("ShortUrlService.incrementTotalClicks: Incrementing total clicks for short code: {}", shortCode);
        shortUrlRepository.incrementTotalClicks(shortCode);
    }

    @Transactional
    public Boolean isUrlAccessible(ShortUrl shortUrl, String password) {
        // if (shortUrl.getStatus() == ShortUrlStatus.DELETED || shortUrl.getStatus() == ShortUrlStatus.DISABLED) {
        //     log.warn("ShortUrlService.isUrlAccessible: URL with code {} is deleted or disabled", shortUrl.getShortCode());
        //     return false;
        // }

        if (shortUrl.getExpiresAt() != null && Instant.now().isAfter(shortUrl.getExpiresAt())) {
            log.warn("ShortUrlService.isUrlAccessible: URL with code {} has expired", shortUrl.getShortCode());
            return false;
        }

        if (shortUrl.getMaxUsage() != null && shortUrl.getTotalClicks() >= shortUrl.getMaxUsage()) {
            log.warn("ShortUrlService.isUrlAccessible: URL with code {} has reached max usage", shortUrl.getShortCode());
            return false;
        }

        if (shortUrl.getPassword() != null) {
            return password != null && passwordEncoder.matches(password, shortUrl.getPassword());
        }

        return true;
    }

    @Transactional
    public ShortUrl updateShortUrl(Long userId, String shortCode, String description, Long maxUsage, Integer availableDays) {
        Optional<ShortUrl> shortUrlOpt = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrlOpt.isEmpty() || !shortUrlOpt.get().getUser().getId().equals(userId)) {
            log.warn("ShortUrlService.updateShortUrl: Short URL with code {} not found for user ID {}", shortCode, userId);
            throw new IllegalArgumentException("Short URL not found or does not belong to the user");
        }
        
        ShortUrl shortUrl = shortUrlOpt.get();
        
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
        
        return shortUrlRepository.save(shortUrl);
    }

    @Transactional
    public ShortUrl updatePassword(Long userId, String shortCode, String newPassword, String currentPassword) {
        Optional<ShortUrl> shortUrlOpt = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrlOpt.isEmpty() || !shortUrlOpt.get().getUser().getId().equals(userId)) {
            log.warn("ShortUrlService.updatePassword: Short URL with code {} not found for user ID {}", shortCode, userId);
            throw new IllegalArgumentException("Short URL not found or does not belong to the user");
        }
        
        ShortUrl shortUrl = shortUrlOpt.get();
        
        // Verify current password if URL already has a password
        if (shortUrl.getPassword() != null) {
            if (currentPassword == null || !passwordEncoder.matches(currentPassword, shortUrl.getPassword())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
        }
        
        // Set new password
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            shortUrl.setPassword(passwordEncoder.encode(newPassword));
        } else {
            shortUrl.setPassword(null); // Remove password
        }
        
        return shortUrlRepository.save(shortUrl);
    }

    @Transactional(readOnly = true)
    public Optional<ShortUrl> findByShortCodeAndUserId(String shortCode, Long userId) {
        log.debug("ShortUrlService.findByShortCodeAndUserId: Finding short URL with code: {} for user: {}", shortCode, userId);
        Optional<ShortUrl> shortUrlOpt = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrlOpt.isPresent() && shortUrlOpt.get().getUser().getId().equals(userId)) {
            return shortUrlOpt;
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Boolean unlockUrl(String shortCode, String password) {
        log.debug("ShortUrlService.unlockUrl: Attempting to unlock URL with code: {}", shortCode);
        Optional<ShortUrl> shortUrlOpt = shortUrlRepository.findByShortCode(shortCode);
        
        if (shortUrlOpt.isEmpty()) {
            log.warn("ShortUrlService.unlockUrl: Short URL with code {} not found", shortCode);
            return false;
        }
        
        ShortUrl shortUrl = shortUrlOpt.get();
        return isUrlAccessible(shortUrl, password);
    }
}
