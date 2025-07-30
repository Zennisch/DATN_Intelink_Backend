package intelink.controllers;

import intelink.dto.request.CreateShortUrlRequest;
import intelink.dto.response.CreateShortUrlResponse;
import intelink.models.ShortUrl;
import intelink.models.User;
import intelink.services.ShortUrlService;
import intelink.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/url")
public class UrlController {

    private final ShortUrlService shortUrlService;
    private final UserService userService;

    @Value("${app.short-url.password-unlock-url}")
    private String passwordUnlockUrlTemplate;

    @PostMapping
    public ResponseEntity<?> createShortUrl(
            @Valid
            @RequestBody CreateShortUrlRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IllegalBlockSizeException, BadPaddingException {
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        ShortUrl shortUrl = shortUrlService.create(user, request);
        CreateShortUrlResponse response = CreateShortUrlResponse.fromEntity(shortUrl, passwordUnlockUrlTemplate);
        return ResponseEntity.ok(response);
    }
}