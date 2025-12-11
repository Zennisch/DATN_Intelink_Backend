package intelink.modules.url.controllers;

import intelink.dto.PagedResponse;
import intelink.dto.url.*;
import intelink.models.ShortUrl;
import intelink.models.ShortUrlAccessControl;
import intelink.models.User;
import intelink.modules.auth.services.AuthService;
import intelink.modules.url.services.ShortUrlAccessControlService;
import intelink.modules.url.services.ShortUrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    private final AuthService authService;

    @Value("${app.url.template.access-url}")
    private String accessUrlTemplate;

    @PostMapping
    public ResponseEntity<?> createShortUrl(@Valid @RequestBody CreateShortUrlRequest request, @AuthenticationPrincipal UserDetails userDetails) throws IllegalBlockSizeException, BadPaddingException {
        User user = authService.getCurrentUser(userDetails);
        ShortUrl shortUrl = shortUrlService.createShortUrl(user, request);
        List<ShortUrlAccessControl> shortUrlAccessControls = shortUrlAccessControlService.getShortUrlAccessControls(shortUrl);
        CreateShortUrlResponse response = CreateShortUrlResponse.fromEntity(shortUrl, shortUrlAccessControls, accessUrlTemplate);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getShortUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getCurrentUser(userDetails);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ShortUrl> shortUrlPage = shortUrlService.getShortUrlsByUser(user, pageable);
        
        Page<ShortUrlResponse> responsePage = shortUrlPage.map(shortUrl -> {
            List<ShortUrlAccessControl> accessControls = shortUrlAccessControlService.getShortUrlAccessControls(shortUrl);
            return ShortUrlResponse.fromEntity(shortUrl, accessControls, accessUrlTemplate);
        });
        
        PagedResponse<ShortUrlResponse> response = PagedResponse.from(responsePage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchShortUrls(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getCurrentUser(userDetails);
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<ShortUrl> shortUrlPage = shortUrlService.searchShortUrls(user, query, pageable);
        
        Page<ShortUrlResponse> responsePage = shortUrlPage.map(shortUrl -> {
            List<ShortUrlAccessControl> accessControls = shortUrlAccessControlService.getShortUrlAccessControls(shortUrl);
            return ShortUrlResponse.fromEntity(shortUrl, accessControls, accessUrlTemplate);
        });
        
        PagedResponse<ShortUrlResponse> response = PagedResponse.from(responsePage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> getShortUrl(@PathVariable String shortCode, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails);
        ShortUrl shortUrl = shortUrlService.getShortUrlByShortCode(user, shortCode);
        List<ShortUrlAccessControl> accessControls = shortUrlAccessControlService.getShortUrlAccessControls(shortUrl);
        ShortUrlResponse response = ShortUrlResponse.fromEntity(shortUrl, accessControls, accessUrlTemplate);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{shortCode}")
    public ResponseEntity<?> updateShortUrl(
            @PathVariable String shortCode,
            @Valid @RequestBody CreateShortUrlRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getCurrentUser(userDetails);
        ShortUrl shortUrl = shortUrlService.updateShortUrl(user, shortCode, request);
        List<ShortUrlAccessControl> accessControls = shortUrlAccessControlService.getShortUrlAccessControls(shortUrl);
        UpdateShortUrlResponse response = UpdateShortUrlResponse.fromEntity(shortUrl, accessControls, accessUrlTemplate);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<?> deleteShortUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getCurrentUser(userDetails);
        shortUrlService.deleteShortUrl(user, shortCode);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{shortCode}/enable")
    public ResponseEntity<?> enableShortUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getCurrentUser(userDetails);
        ShortUrl shortUrl = shortUrlService.enableShortUrl(user, shortCode);
        List<ShortUrlAccessControl> accessControls = shortUrlAccessControlService.getShortUrlAccessControls(shortUrl);
        ShortUrlResponse response = ShortUrlResponse.fromEntity(shortUrl, accessControls, accessUrlTemplate);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{shortCode}/disable")
    public ResponseEntity<?> disableShortUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getCurrentUser(userDetails);
        ShortUrl shortUrl = shortUrlService.disableShortUrl(user, shortCode);
        List<ShortUrlAccessControl> accessControls = shortUrlAccessControlService.getShortUrlAccessControls(shortUrl);
        ShortUrlResponse response = ShortUrlResponse.fromEntity(shortUrl, accessControls, accessUrlTemplate);
        return ResponseEntity.ok(response);
    }

}
