package intelink.models;

import intelink.models.enums.DimensionType;
import java.util.UUID;

public class DimensionStat {
    public UUID id;
    public ShortUrl shortUrl;
    public DimensionType type;
    public String value;
    public Long totalClicks;
    public Long allowedClicks;
    public Long blockedClicks;
}
