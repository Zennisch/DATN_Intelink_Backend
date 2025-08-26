package intelink.controllers;

import intelink.dto.request.UnlockUrlRequest;
import intelink.dto.response.UnlockUrlResponse;
import intelink.exceptions.IncorrectPasswordException;
import intelink.exceptions.ShortUrlUnavailableException;
import intelink.models.ShortUrl;
import intelink.services.ClickLogService;
import intelink.services.ShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    private final ShortUrlService shortUrlService;
    private final ClickLogService clickLogService;

    @Value("${app.url.password-unlock}")
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

    @GetMapping("/{shortCode}/unlock")
    public ResponseEntity<?> getUnlockInfo(@PathVariable String shortCode) {
        Optional<ShortUrl> shortUrlOpt = shortUrlService.findByShortCode(shortCode);
        
        if (shortUrlOpt.isEmpty()) {
            log.warn("Short URL not found for unlock info: {}", shortCode);
            return ResponseEntity.notFound().build();
        }

        ShortUrl shortUrl = shortUrlOpt.get();
        
        // Check if URL requires password
        if (shortUrl.getPassword() == null) {
            log.warn("URL does not require password: {}", shortCode);
            return ResponseEntity.badRequest()
                    .body(UnlockUrlResponse.failure("This URL does not require a password", shortCode));
        }

        // Return info about the URL (without sensitive data)
        return ResponseEntity.ok(UnlockUrlResponse.builder()
                .success(true)
                .message("Password required for this URL")
                .shortCode(shortCode)
                .build());
    }

    @PostMapping("/{shortCode}/unlock")
    public ResponseEntity<?> unlockUrl(
            @PathVariable String shortCode,
            @Valid @RequestBody UnlockUrlRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            Optional<ShortUrl> shortUrlOpt = shortUrlService.findByShortCode(shortCode);
            
            if (shortUrlOpt.isEmpty()) {
                log.warn("Short URL not found for unlock: {}", shortCode);
                return ResponseEntity.badRequest()
                        .body(UnlockUrlResponse.failure("Short URL not found", shortCode));
            }

            ShortUrl shortUrl = shortUrlOpt.get();
            boolean isAccessible = shortUrlService.unlockUrl(shortCode, request.getPassword());
            
            if (isAccessible) {
                // Record click log
                clickLogService.record(shortCode, httpRequest);
                
                log.info("URL unlocked successfully: {}", shortCode);
                return ResponseEntity.ok(
                    UnlockUrlResponse.success(shortUrl.getOriginalUrl(), shortCode)
                );
            } else {
                log.warn("Failed to unlock URL - incorrect password or URL unavailable: {}", shortCode);
                return ResponseEntity.badRequest()
                        .body(UnlockUrlResponse.failure("Incorrect password or URL is unavailable", shortCode));
            }
        } catch (Exception e) {
            log.error("Error unlocking URL: {}", shortCode, e);
            return ResponseEntity.internalServerError()
                    .body(UnlockUrlResponse.failure("An error occurred while unlocking the URL", shortCode));
        }
    }
}