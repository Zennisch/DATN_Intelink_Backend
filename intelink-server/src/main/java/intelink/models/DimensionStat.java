package intelink.models;

import intelink.models.enums.DimensionType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class DimensionStat {
    public UUID id;
    public ShortUrl shortUrl;
    public DimensionType type;
    public String value;
    public Long totalClicks;
    public Long allowedClicks;
    public Long blockedClicks;
}
