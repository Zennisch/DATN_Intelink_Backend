package intelink.modules.url.controllers;

import intelink.dto.url.CreateShortUrlRequest;
import intelink.dto.url.CreateShortUrlResponse;
import intelink.models.ShortUrl;
import intelink.models.ShortUrlAccessControl;
import intelink.models.User;
import intelink.modules.auth.services.UserService;
import intelink.modules.url.services.ShortUrlAccessControlService;
import intelink.modules.url.services.ShortUrlService;
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
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/url")
public class ShortUrlController {

    private final ShortUrlService shortUrlService;
    private final ShortUrlAccessControlService shortUrlAccessControlService;
    private final UserService userService;

    @Value("${app.url.template.access-url}")
    private String accessUrlTemplate;

    @PostMapping
    public ResponseEntity<?> createShortUrl(
            @Valid @RequestBody CreateShortUrlRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IllegalBlockSizeException, BadPaddingException {
        User user = userService.getCurrentUser(userDetails);
        ShortUrl shortUrl = shortUrlService.createShortUrl(user, request);
        List<ShortUrlAccessControl> shortUrlAccessControls = shortUrlAccessControlService.getShortUrlAccessControls(shortUrl);
        CreateShortUrlResponse response = CreateShortUrlResponse.fromEntity(shortUrl, shortUrlAccessControls, accessUrlTemplate);
        return ResponseEntity.ok(response);
    }

}
