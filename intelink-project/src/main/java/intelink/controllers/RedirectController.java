package intelink.controllers;

import intelink.dto.request.url.UnlockUrlRequest;
import intelink.dto.response.url.UnlockUrlResponse;
import intelink.dto.response.redirect.RedirectResult;
import intelink.exceptions.IncorrectPasswordException;
import intelink.exceptions.ShortUrlUnavailableException;
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
        UnlockUrlResponse response = shortUrlService.getUnlockInfo(shortCode);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{shortCode}/unlock")
    public ResponseEntity<?> unlockUrl(
            @PathVariable String shortCode,
            @Valid @RequestBody UnlockUrlRequest request,
            HttpServletRequest httpRequest
    ) {
        UnlockUrlResponse response = shortUrlService.unlockUrl(shortCode, request.getPassword(), httpRequest);
        if (!response.getSuccess()) {
            log.warn("ShortUrlService.unlockUrl: Failed to unlock URL: {}. Reason: {}", shortCode, response.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        clickLogService.record(shortCode, httpRequest);
        log.info("ShortUrlService.unlockUrl: URL unlocked successfully: {}", shortCode);
        // Trả về JSON thay vì redirect
        return ResponseEntity.ok(response);
    }
}