package intelink.models.news;

import intelink.models.news.enums.ShortUrlStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "short_urls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class ShortUrl {

    // Key group
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    // Info group
    @Column(name = "short_code", nullable = false, unique = true)
    private String shortCode;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "password", nullable = true, length = 255)
    private String passwordHash;

    @Column(name = "alias", nullable = true, length = 64)
    private String alias;

    @Column(name = "description", nullable = true, length = 255)
    private String description;

    // Stats group
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ShortUrlStatus status = ShortUrlStatus.ENABLED;

    @Column(name = "max_usage", nullable = true)
    @Builder.Default
    private Long maxUsage = 0L;

    @Column(name = "total_clicks", nullable = false)
    @Builder.Default
    private Long totalClicks = 0L;

    // Audit group
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "expires_at", nullable = true)
    private Instant expiresAt;

    @Column(name = "deleted_at", nullable = true)
    private Instant deletedAt;

    // Lifecycle hooks
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
