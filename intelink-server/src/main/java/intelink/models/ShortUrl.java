package intelink.models;

import java.time.Instant;

public class ShortUrl {
    public Long id;
    public User user;
    
    public String title;
    public String description;
    
    public String originalUrl;
    public String shortCode;
    
    public Boolean enabled;
    public Integer maxUsage;
    
    public Instant expiresAt;
    public Instant deletedAt;
    
    public Long totalClicks;
    public Long allowedClicks;
    public Long blockedClicks;
    public Long uniqueClicks;
    
    public Instant createdAt;
    public Instant updatedAt;
}
