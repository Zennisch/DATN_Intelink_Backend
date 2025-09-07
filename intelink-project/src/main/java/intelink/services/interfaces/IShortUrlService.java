package intelink.services.interfaces;

import intelink.dto.request.url.CreateShortUrlRequest;
import intelink.dto.response.url.UnlockUrlResponse;
import intelink.models.ShortUrl;
import intelink.models.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.util.Optional;

public interface IShortUrlService {

    // Create operations
    ShortUrl create(User user, CreateShortUrlRequest request) throws IllegalBlockSizeException, BadPaddingException;

    // Status management operations
    void enableShortUrl(Long userId, String shortCode);
    void disableShortUrl(Long userId, String shortCode);
    void deleteShortUrl(Long userId, String shortCode);

    // Read operations
    Optional<ShortUrl> findByShortCode(String shortCode);
    Optional<ShortUrl> findByShortCodeAndUserId(String shortCode, Long userId);
    Page<ShortUrl> getUserShortUrls(Long userId, Pageable pageable);
    Page<ShortUrl> getUserShortUrlsWithSorting(Long userId, Pageable pageable);

    // Update operations
    ShortUrl updateShortUrl(Long userId, String shortCode, String description, Long maxUsage, Integer availableDays);
    ShortUrl updatePassword(Long userId, String shortCode, String newPassword, String currentPassword);

    // Access and unlock operations
    Boolean isUrlAccessible(ShortUrl shortUrl, String password);
    UnlockUrlResponse getUnlockInfo(String shortCode);
    UnlockUrlResponse unlockUrl(String shortCode, String password, HttpServletRequest request);
    
    // Helper method
    User getCurrentUser(UserDetails userDetails);
}