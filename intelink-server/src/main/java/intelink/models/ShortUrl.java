package intelink.models;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
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

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
