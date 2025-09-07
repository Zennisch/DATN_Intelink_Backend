package intelink.controllers;

import intelink.dto.request.UnlockUrlRequest;
import intelink.dto.response.UnlockUrlResponse;
import intelink.dto.response.redirect.RedirectResult;
import intelink.exceptions.IncorrectPasswordException;
import intelink.exceptions.ShortUrlUnavailableException;
import intelink.models.ShortUrl;
import intelink.services.interfaces.IClickLogService;
import intelink.services.interfaces.IRedirectService;
import intelink.services.interfaces.IShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final IRedirectService redirectService;
    private final IShortUrlService shortUrlService;
    private final IClickLogService clickLogService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirect(
            @PathVariable String shortCode,
            @RequestParam(required = false) String password,
            HttpServletRequest request
    ) throws NoResourceFoundException {

        RedirectResult result = redirectService.handleRedirect(shortCode, password, request);

        return switch (result.getType()) {
            case SUCCESS, PASSWORD_REQUIRED -> ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", result.getRedirectUrl())
                    .build();
            case NOT_FOUND -> throw new NoResourceFoundException(HttpMethod.GET, result.getErrorMessage());
            case UNAVAILABLE -> throw new ShortUrlUnavailableException(result.getErrorMessage());
            case INCORRECT_PASSWORD -> throw new IncorrectPasswordException(result.getErrorMessage());
        };
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
        if (shortUrl.getPasswordHash() == null) {
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