package intelink.dto.response.url;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateShortUrlResponse {
    private String message;
    private String shortCode;
    private Boolean success;

    public static UpdateShortUrlResponse success(String shortCode) {
        return UpdateShortUrlResponse.builder()
                .message("Short URL updated successfully")
                .shortCode(shortCode)
                .success(true)
                .build();
    }

    public static UpdateShortUrlResponse failure(String message) {
        return UpdateShortUrlResponse.builder()
                .message(message)
                .success(false)
                .build();
    }
}
