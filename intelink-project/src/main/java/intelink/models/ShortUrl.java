package intelink.models;

import intelink.models.enums.ShortUrlStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "short_urls", indexes = {
        @Index(name = "idx_short_urls_short_code", columnList = "short_code", unique = true),
        @Index(name = "idx_short_urls_alias", columnList = "alias"),
        @Index(name = "idx_short_urls_user", columnList = "user_id"),
        @Index(name = "idx_short_urls_status", columnList = "status"),
        @Index(name = "idx_short_urls_expires_at", columnList = "expires_at"),
        @Index(name = "idx_short_urls_created_at", columnList = "created_at"),
        @Index(name = "idx_short_urls_deleted_at", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class ShortUrl {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "short_code", nullable = false, unique = true)
    private String shortCode;

    @Column(name = "alias", nullable = true, length = 100)
    private String alias;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "password", nullable = true, length = 255)
    private String password;

    @Column(name = "description", nullable = true, length = 255)
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShortUrlStatus status = ShortUrlStatus.ENABLED;

    @Builder.Default
    @Column(name = "max_usage", nullable = true)
    private Long maxUsage = 0L;

    @Builder.Default
    @Column(name = "total_clicks", nullable = false)
    private Long totalClicks = 0L;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Soft delete fields
    @Column(name = "deleted_at", nullable = true)
    private Instant deletedAt;

    @Column(name = "deleted_by", nullable = true)
    private Long deletedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_domain_id", nullable = true)
    @ToString.Exclude
    private CustomDomain customDomain;

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Utility methods
    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isActive() {
        return !isDeleted() && status == ShortUrlStatus.ENABLED;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isAccessible() {
        return isActive() && !isExpired();
    }

    public void softDelete(Long deletedByUserId) {
        this.deletedAt = Instant.now();
        this.deletedBy = deletedByUserId;
    }

    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }

}
