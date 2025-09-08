package intelink.controllers;

import intelink.dto.request.url.CreateShortUrlRequest;
import intelink.dto.request.url.UpdatePasswordRequest;
import intelink.dto.request.url.UpdateShortUrlRequest;
import intelink.dto.response.url.CreateShortUrlResponse;
import intelink.dto.response.url.ShortUrlDetailResponse;
import intelink.dto.response.url.ShortUrlListResponse;
import intelink.dto.response.PagedResponse;
import intelink.dto.response.url.UpdateShortUrlResponse;
import intelink.models.ShortUrl;
import intelink.models.User;
import intelink.services.interfaces.IShortUrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/url")
public class UrlController {

    private final IShortUrlService shortUrlService;

    @Value("${app.url.access}")
    private String accessUrl;

    @PostMapping
    public ResponseEntity<?> createShortUrl(
            @Valid @RequestBody CreateShortUrlRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IllegalBlockSizeException, BadPaddingException {
        User user = shortUrlService.getCurrentUser(userDetails);
        ShortUrl shortUrl = shortUrlService.create(user, request);
        CreateShortUrlResponse response = CreateShortUrlResponse.fromEntity(shortUrl, accessUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getUserShortUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = shortUrlService.getCurrentUser(userDetails);
        Pageable pageable = PageRequest.of(page, size);
        Page<ShortUrl> shortUrlPage = shortUrlService.getUserShortUrls(user.getId(), pageable);
        
        Page<ShortUrlListResponse> responsePage = shortUrlPage.map(shortUrl -> 
            ShortUrlListResponse.fromEntity(shortUrl, accessUrl)
        );
        
        PagedResponse<ShortUrlListResponse> response = PagedResponse.from(responsePage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> getShortUrlDetail(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = shortUrlService.getCurrentUser(userDetails);
        Optional<ShortUrl> shortUrlOpt = shortUrlService.findByShortCodeAndUserId(shortCode, user.getId());
        
        if (shortUrlOpt.isEmpty()) {
            throw new IllegalArgumentException("Short URL not found");
        }

        ShortUrl shortUrl = shortUrlOpt.get();
        ShortUrlDetailResponse response = ShortUrlDetailResponse.fromEntity(shortUrl, accessUrl);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{shortCode}")
    public ResponseEntity<?> updateShortUrl(
            @PathVariable String shortCode,
            @Valid @RequestBody UpdateShortUrlRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = shortUrlService.getCurrentUser(userDetails);
        ShortUrl updatedUrl = shortUrlService.updateShortUrl(
            user.getId(),
            shortCode,
            request.getDescription(),
            request.getMaxUsage(),
            request.getAvailableDays()
        );
        UpdateShortUrlResponse response = UpdateShortUrlResponse.success(updatedUrl.getShortCode());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<?> deleteShortUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = shortUrlService.getCurrentUser(userDetails);
        shortUrlService.deleteShortUrl(user.getId(), shortCode);
        
        UpdateShortUrlResponse response = UpdateShortUrlResponse.builder()
            .message("Short URL deleted successfully")
            .shortCode(shortCode)
            .success(true)
            .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{shortCode}/enable")
    public ResponseEntity<?> enableShortUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = shortUrlService.getCurrentUser(userDetails);
        shortUrlService.enableShortUrl(user.getId(), shortCode);
        
        UpdateShortUrlResponse response = UpdateShortUrlResponse.builder()
            .message("Short URL enabled successfully")
            .shortCode(shortCode)
            .success(true)
            .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{shortCode}/disable")
    public ResponseEntity<?> disableShortUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = shortUrlService.getCurrentUser(userDetails);
        shortUrlService.disableShortUrl(user.getId(), shortCode);
        
        UpdateShortUrlResponse response = UpdateShortUrlResponse.builder()
            .message("Short URL disabled successfully")
            .shortCode(shortCode)
            .success(true)
            .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{shortCode}/password")
    public ResponseEntity<?> updatePassword(
            @PathVariable String shortCode,
            @Valid @RequestBody UpdatePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = shortUrlService.getCurrentUser(userDetails);
        ShortUrl updatedUrl = shortUrlService.updatePassword(
            user.getId(),
            shortCode,
            request.getNewPassword(),
            request.getCurrentPassword()
        );
        
        UpdateShortUrlResponse response = UpdateShortUrlResponse.builder()
            .message("Password updated successfully")
            .shortCode(updatedUrl.getShortCode())
            .success(true)
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchShortUrls(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = shortUrlService.getCurrentUser(userDetails);
        
        // Create Pageable with sorting
        Pageable pageable;
        if ("asc".equalsIgnoreCase(sortDirection)) {
            pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sortBy).ascending());
        } else {
            pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sortBy).descending());
        }
        
        // Use the new search method that supports query and status filtering
        Page<ShortUrl> shortUrlPage = shortUrlService.searchShortUrls(user.getId(), query, status, pageable);
        
        Page<ShortUrlListResponse> responsePage = shortUrlPage.map(shortUrl -> 
            ShortUrlListResponse.fromEntity(shortUrl, accessUrl)
        );
        
        PagedResponse<ShortUrlListResponse> response = PagedResponse.from(responsePage);
        return ResponseEntity.ok(response);
    }
}