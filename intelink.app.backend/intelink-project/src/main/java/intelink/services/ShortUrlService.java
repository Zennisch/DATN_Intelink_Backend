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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.time.Instant;

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

        log.info("ShortUrlService.create: Created short URL with code: {} for user: {}", shortCode, user.getUsername());
        return savedUrl;
    }
}
