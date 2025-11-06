package intelink.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder

@Entity
@Table(name = "short_urls")
public class ShortUrl {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Column(name = "title", length = 32)
    public String title;

    @Column(name = "description")
    public String description;

    @Column(name = "original_url", nullable = false, length = 2048)
    public String originalUrl;

    @Column(name = "short_code", nullable = false, unique = true, length = 16)
    public String shortCode;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    public Boolean enabled = true;

    @Column(name = "max_usage")
    public Integer maxUsage;

    @Column(name = "expires_at")
    public Instant expiresAt;

    @Column(name = "deleted_at")
    public Instant deletedAt;

    @Column(name = "total_clicks", nullable = false)
    @Builder.Default
    public Long totalClicks = 0L;

    @Column(name = "allowed_clicks", nullable = false)
    @Builder.Default
    public Long allowedClicks = 0L;

    @Column(name = "blocked_clicks", nullable = false)
    @Builder.Default
    public Long blockedClicks = 0L;

    @Column(name = "unique_clicks", nullable = false)
    @Builder.Default
    public Long uniqueClicks = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
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
