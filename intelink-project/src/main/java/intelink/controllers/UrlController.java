package intelink.controllers;

import intelink.dto.request.CreateShortUrlRequest;
import intelink.dto.request.UpdatePasswordRequest;
import intelink.dto.request.UpdateShortUrlRequest;
import intelink.dto.response.CreateShortUrlResponse;
import intelink.dto.response.ShortUrlDetailResponse;
import intelink.dto.response.ShortUrlListResponse;
import intelink.dto.response.PagedResponse;
import intelink.dto.response.UpdateShortUrlResponse;
import intelink.models.ShortUrl;
import intelink.models.User;
import intelink.models.enums.ShortUrlStatus;
import intelink.services.ShortUrlService;
import intelink.services.UserService;
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

    private final ShortUrlService shortUrlService;
    private final UserService userService;

    @Value("${app.url.access}")
    private String accessUrl;

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
        // if (shortUrl.getStatus() == ShortUrlStatus.DELETED) {
        //     throw new IllegalArgumentException("The URL is unsafe and has been deleted");
        // }
        CreateShortUrlResponse response = CreateShortUrlResponse.fromEntity(shortUrl, accessUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getUserShortUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
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
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        Optional<ShortUrl> shortUrlOpt = shortUrlService.findByShortCodeAndUserId(shortCode, user.getId());
        if (shortUrlOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
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
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        try {
            ShortUrl updatedUrl = shortUrlService.updateShortUrl(
                user.getId(),
                shortCode,
                request.getDescription(),
                request.getMaxUsage(),
                request.getAvailableDays()
            );
            UpdateShortUrlResponse response = UpdateShortUrlResponse.success(updatedUrl.getShortCode());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            UpdateShortUrlResponse response = UpdateShortUrlResponse.failure(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<?> deleteShortUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        try {
            shortUrlService.deleteShortUrl(user.getId(), shortCode);
            UpdateShortUrlResponse response = UpdateShortUrlResponse.builder()
                .message("Short URL deleted successfully")
                .shortCode(shortCode)
                .success(true)
                .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            UpdateShortUrlResponse response = UpdateShortUrlResponse.failure(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{shortCode}/enable")
    public ResponseEntity<?> enableShortUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        try {
            shortUrlService.enableShortUrl(user.getId(), shortCode);
            UpdateShortUrlResponse response = UpdateShortUrlResponse.builder()
                .message("Short URL enabled successfully")
                .shortCode(shortCode)
                .success(true)
                .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            UpdateShortUrlResponse response = UpdateShortUrlResponse.failure(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{shortCode}/disable")
    public ResponseEntity<?> disableShortUrl(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        try {
            shortUrlService.disableShortUrl(user.getId(), shortCode);
            UpdateShortUrlResponse response = UpdateShortUrlResponse.builder()
                .message("Short URL disabled successfully")
                .shortCode(shortCode)
                .success(true)
                .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            UpdateShortUrlResponse response = UpdateShortUrlResponse.failure(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{shortCode}/password")
    public ResponseEntity<?> updatePassword(
            @PathVariable String shortCode,
            @Valid @RequestBody UpdatePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        try {
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
        } catch (IllegalArgumentException e) {
            UpdateShortUrlResponse response = UpdateShortUrlResponse.failure(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
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
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        
        // Tạo Pageable với sorting
        Pageable pageable;
        if ("asc".equalsIgnoreCase(sortDirection)) {
            pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sortBy).ascending());
        } else {
            pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sortBy).descending());
        }
        
        // Sử dụng method mới với custom sorting
        Page<ShortUrl> shortUrlPage = shortUrlService.getUserShortUrlsWithSorting(user.getId(), pageable);
        
        Page<ShortUrlListResponse> responsePage = shortUrlPage.map(shortUrl -> 
            ShortUrlListResponse.fromEntity(shortUrl, accessUrl)
        );
        
        PagedResponse<ShortUrlListResponse> response = PagedResponse.from(responsePage);
        return ResponseEntity.ok(response);
    }
}