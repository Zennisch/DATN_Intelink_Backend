
package intelink.dto.request.api_key;

import lombok.Data;

@Data
public class CreateApiKeyRequest {
    private String name;
    private Integer rateLimitPerHour;
    private Boolean active;
}
