package intelink.controllers;

import intelink.exceptions.IncorrectPasswordException;
import intelink.exceptions.ShortUrlUnavailableException;
import intelink.models.ShortUrl;
import intelink.services.ClickLogService;
import intelink.services.ShortUrlService;
import intelink.utils.GoogleSafeBrowsingUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    private final ShortUrlService shortUrlService;
    private final ClickLogService clickLogService;

    @Value("${app.short-url.password-unlock-url}")
    private String passwordUnlockUrlTemplate;

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirect(
            @PathVariable String shortCode,
            @RequestParam(required = false) String password,
            HttpServletRequest request
    ) throws NoResourceFoundException {
        Optional<ShortUrl> shortUrlOpt = shortUrlService.findByShortCode(shortCode);

        if (shortUrlOpt.isEmpty()) {
            log.warn("Short URL not found: {}", shortCode);
            throw new NoResourceFoundException(HttpMethod.GET, "Short URL not found: " + shortCode);
        }

        ShortUrl shortUrl = shortUrlOpt.get();
        if (!shortUrlService.isUrlAccessible(shortUrl, password)) {
            if (shortUrl.getPassword() != null) {
                if (password == null) {
                    String unlockUrl = passwordUnlockUrlTemplate.replace("{shortCode}", shortCode);
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .header("Location", unlockUrl)
                            .build();
                } else {
                    throw new IncorrectPasswordException("Incorrect password for short URL: " + shortCode);
                }
            }
            throw new ShortUrlUnavailableException("URL is no longer accessible: " + shortCode);
        }

        clickLogService.record(shortCode, request);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", shortUrl.getOriginalUrl())
                .build();
    }
}