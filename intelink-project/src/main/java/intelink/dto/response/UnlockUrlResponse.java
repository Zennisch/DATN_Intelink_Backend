package intelink.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnlockUrlResponse {
    private Boolean success;
    private String message;
    private String redirectUrl;
    private String shortCode;

    public static UnlockUrlResponse success(String redirectUrl, String shortCode) {
        return UnlockUrlResponse.builder()
                .success(true)
                .message("URL unlocked successfully")
                .redirectUrl(redirectUrl)
                .shortCode(shortCode)
                .build();
    }

    public static UnlockUrlResponse failure(String message, String shortCode) {
        return UnlockUrlResponse.builder()
                .success(false)
                .message(message)
                .shortCode(shortCode)
                .build();
    }
}
