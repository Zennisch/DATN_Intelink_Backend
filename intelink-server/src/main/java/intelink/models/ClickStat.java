package intelink.models;

import intelink.models.enums.Granularity;
import java.time.Instant;
import java.util.UUID;

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
