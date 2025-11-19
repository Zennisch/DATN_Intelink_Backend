package intelink.controllers;

import intelink.dto.url.CreateShortUrlRequest;
import intelink.models.ShortUrl;
import intelink.models.User;
import intelink.modules.url.ShortUrlService;
import intelink.modules.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/url")
public class ShortUrlController {

    private final ShortUrlService shortUrlService;
    private final UserService userService;

    @Value("${app.url.access-url}")
    private String accessUrl;

    @PostMapping
    public ResponseEntity<?> createShortUrl(
            @Valid @RequestBody CreateShortUrlRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IllegalBlockSizeException, BadPaddingException {
        User user = userService.getCurrentUser(userDetails);
        ShortUrl shortUrl = shortUrlService.createShortUrl(user, request);
        CreateShortUrlResponse response = CreateShortUrlResponse.fromEntity(shortUrl, accessUrl);
        return ResponseEntity.ok(response);
    }

}
