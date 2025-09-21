package intelink.dto.response.redirect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedirectResult {
    private RedirectType type;
    private String redirectUrl;
    private String errorMessage;
    private String shortCode;

    public static RedirectResult success(String redirectUrl) {
        return RedirectResult.builder()
                .type(RedirectType.SUCCESS)
                .redirectUrl(redirectUrl)
                .build();
    }

    public static RedirectResult passwordRequired(String unlockUrl, String shortCode) {
        return RedirectResult.builder()
                .type(RedirectType.PASSWORD_REQUIRED)
                .redirectUrl(unlockUrl)
                .shortCode(shortCode)
                .build();
    }

    public static RedirectResult notFound(String shortCode) {
        return RedirectResult.builder()
                .type(RedirectType.NOT_FOUND)
                .shortCode(shortCode)
                .errorMessage("Short URL not found: " + shortCode)
                .build();
    }

    public static RedirectResult unavailable(String shortCode) {
        return RedirectResult.builder()
                .type(RedirectType.UNAVAILABLE)
                .shortCode(shortCode)
                .errorMessage("URL is no longer accessible: " + shortCode)
                .build();
    }

    public static RedirectResult incorrectPassword(String shortCode) {
        return RedirectResult.builder()
                .type(RedirectType.INCORRECT_PASSWORD)
                .shortCode(shortCode)
                .errorMessage("Incorrect password for short URL: " + shortCode)
                .build();
    }

    public enum RedirectType {
        SUCCESS,           // Redirect to original URL
        PASSWORD_REQUIRED, // Redirect to password unlock page
        NOT_FOUND,         // Short URL not found
        UNAVAILABLE,       // URL is expired/disabled/max usage reached
        INCORRECT_PASSWORD // Wrong password provided
    }
}