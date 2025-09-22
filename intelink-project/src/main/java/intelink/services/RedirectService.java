package intelink.services;

import intelink.dto.object.DimensionInfo;
import intelink.dto.response.redirect.RedirectResult;
import intelink.models.ClickStat;
import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.enums.DimensionType;
import intelink.models.enums.Granularity;
import intelink.repositories.ClickStatRepository;
import intelink.repositories.DimensionStatRepository;
import intelink.services.interfaces.IClickLogService;
import intelink.services.interfaces.IRedirectService;
import intelink.services.interfaces.IShortUrlService;
import intelink.utils.DateTimeUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedirectService implements IRedirectService {

    private final IShortUrlService shortUrlService;
    private final IClickLogService clickLogService;

    @Value("${app.url.password-unlock}")
    private String passwordUnlockUrlTemplate;

    private static <K, V> Map.Entry<K, V> entry(K k, V v) {
        return new AbstractMap.SimpleEntry<>(k, v);
    }

    @Transactional
    public RedirectResult handleRedirect(String shortCode, String password, HttpServletRequest request) {
        // 1. Find short URL by code
        Optional<ShortUrl> shortUrlOpt = shortUrlService.findByShortCode(shortCode);
        if (shortUrlOpt.isEmpty()) {
            log.warn("RedirectService.handleRedirect: Short URL not found: {}", shortCode);
            return RedirectResult.notFound(shortCode);
        }

        // 2. Check if URL is accessible
        ShortUrl shortUrl = shortUrlOpt.get();
        if (!shortUrlService.isUrlAccessible(shortUrl, password)) {
            // 2.1. If URL requires password and none provided, redirect to unlock page
            if (shortUrl.getPasswordHash() != null) {
                if (password == null) {
                    String unlockUrl = passwordUnlockUrlTemplate.replace("{shortCode}", shortCode);
                    log.info("RedirectService.handleRedirect: Password required for URL: {}", shortCode);
                    return RedirectResult.passwordRequired(unlockUrl, shortCode);
                } else {
                    log.warn("RedirectService.handleRedirect: Incorrect password for URL: {}", shortCode);
                    return RedirectResult.incorrectPassword(shortCode);
                }
            }
            // 2.2. URL is unavailable (expired, disabled, max usage reached)
            log.warn("RedirectService.handleRedirect: URL unavailable: {}", shortCode);
            return RedirectResult.unavailable(shortCode);
        }

        // 3. Record click log and redirect to original URL
        clickLogService.record(shortCode, request);
        log.info("RedirectService.handleRedirect: Successful redirect for: {}", shortCode);
        return RedirectResult.success(shortUrl.getOriginalUrl());
    }
}