package intelink.controllers;

import intelink.dto.CreateShortUrlRequest;
import intelink.dto.ShortUrlResponse;
import intelink.models.ShortUrl;
import intelink.models.User;
import intelink.services.ShortUrlService;
import intelink.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
@Slf4j
public class UrlController {

    private final ShortUrlService shortUrlService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ShortUrlResponse> createShortUrl(
            @Valid @RequestBody CreateShortUrlRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userOpt.get();
        Timestamp expiresAt = request.getExpiresAt() != null ?
                Timestamp.from(request.getExpiresAt()) : null;

        ShortUrl shortUrl = shortUrlService.createShortUrl(
                request.getOriginalUrl(),
                user,
                request.getDescription(),
                expiresAt,
                request.getMaxUsage(),
                request.getPassword()
        );

        ShortUrlResponse response = ShortUrlResponse.fromEntity(shortUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ShortUrlResponse>> getUserUrls(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {

        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Page<ShortUrl> urls = shortUrlService.getUserUrls(userOpt.get().getId(), pageable);
        Page<ShortUrlResponse> response = urls.map(ShortUrlResponse::fromEntity);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<ShortUrlResponse> getShortUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails) {

        Optional<ShortUrl> shortUrlOpt = shortUrlService.findByShortCode(shortCode);
        if (shortUrlOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ShortUrl shortUrl = shortUrlOpt.get();
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());

        // Check ownership
        if (userOpt.isEmpty() || !shortUrl.getUser().getId().equals(userOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(ShortUrlResponse.fromEntity(shortUrl));
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteShortUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails) {

        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            shortUrlService.deleteShortUrl(shortCode, userOpt.get().getId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}