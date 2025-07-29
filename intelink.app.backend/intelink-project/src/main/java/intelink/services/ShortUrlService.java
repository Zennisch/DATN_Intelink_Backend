package intelink.services;

import intelink.dto.Cipher;
import intelink.dto.request.CreateShortUrlRequest;
import intelink.models.ShortUrl;
import intelink.models.User;
import intelink.models.enums.ShortUrlStatus;
import intelink.repositories.ShortUrlRepository;
import intelink.services.interfaces.IShortUrlService;
import intelink.utils.FPEUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortUrlService implements IShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final UserService userService;

    private final Integer SHORT_CODE_LENGTH = 10;

    @Transactional
    public ShortUrl create(User user, CreateShortUrlRequest request) throws IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = FPEUtil.generate(user.getId(), SHORT_CODE_LENGTH);
        String shortCode = cipher.getText();
        Instant expiresAt = Instant.now().plusSeconds(request.getAvailableDays() * 24 * 60 * 60);
        ShortUrl shortUrl = ShortUrl.builder()
                .shortCode(shortCode)
                .originalUrl(request.getOriginalUrl())
                .password(request.getPassword())
                .description(request.getDescription())
                .status(ShortUrlStatus.ENABLED)
                .maxUsage(request.getMaxUsage())
                .expiresAt(expiresAt)
                .user(user)
                .build();

        ShortUrl savedUrl = shortUrlRepository.save(shortUrl);
        userService.incrementTotalShortUrls(user.getId());

        log.debug("ShortUrlService.create: Created short URL with code: {} for user: {}", shortCode, user.getUsername());
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public void deleteShortUrl(Long userId, String shortCode) {
        Optional<ShortUrl> shortUrlOpt = shortUrlRepository.findByShortCode(shortCode);
        if (shortUrlOpt.isEmpty() || !shortUrlOpt.get().getUser().getId().equals(userId)) {
            log.warn("ShortUrlService.deleteShortUrl: Short URL with code {} not found for user ID {}", shortCode, userId);
            throw new IllegalArgumentException("Short URL not found or does not belong to the user");
        }
        ShortUrl shortUrl = shortUrlOpt.get();
        shortUrl.setStatus(ShortUrlStatus.DELETED);
        shortUrlRepository.save(shortUrl);
        userService.decrementTotalShortUrls(userId);
    }

    @Transactional(readOnly = true)
    public Optional<ShortUrl> findByShortCode(String shortCode) {
        log.debug("ShortUrlService.findByShortCode: Finding short URL with code: {}", shortCode);
        return shortUrlRepository.findByShortCode(shortCode);
    }

    @Transactional
    public Page<ShortUrl> getUserShortUrls(Long userId, Pageable pageable) {
        log.debug("ShortUrlService.getUserShortUrls: Fetching short URLs for user ID: {}", userId);
        return shortUrlRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public void incrementTotalClicks(String shortCode) {
        log.debug("ShortUrlService.incrementTotalClicks: Incrementing total clicks for short code: {}", shortCode);
        shortUrlRepository.incrementTotalClicks(shortCode);
    }

}
