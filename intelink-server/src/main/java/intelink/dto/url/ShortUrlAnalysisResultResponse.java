package intelink.dto.url;

import intelink.models.ShortUrlAnalysisResult;
import intelink.models.enums.ShortUrlAnalysisEngine;
import intelink.models.enums.ShortUrlAnalysisStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortUrlAnalysisResultResponse {

    private UUID id;
    private ShortUrlAnalysisStatus status;
    private ShortUrlAnalysisEngine engine;
    private String threatType;
    private String platformType;
    private String cacheDuration;
    private String details;
    private Instant createdAt;

    public static ShortUrlAnalysisResultResponse fromEntity(ShortUrlAnalysisResult entity) {
        return ShortUrlAnalysisResultResponse.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .engine(entity.getEngine())
                .threatType(entity.getThreatType())
                .platformType(entity.getPlatformType())
                .cacheDuration(entity.getCacheDuration())
                .details(entity.getDetails())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
