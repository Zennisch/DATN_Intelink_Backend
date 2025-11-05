package intelink.models;

import intelink.models.enums.Granularity;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class ClickStat {
    public UUID id;
    public ShortUrl shortUrl;
    public Granularity granularity;
    public Instant bucketStart;
    public Instant bucketEnd;
    public Long totalClicks;
    public Long allowedClicks;
    public Long blockedClicks;
}
