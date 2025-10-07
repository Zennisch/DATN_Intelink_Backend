package intelink.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class ApiKey {

    // Key group
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private User user;

    // Information group
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "raw_key", nullable = false, unique = true, length = 255)
    private String rawKey;

    @Column(name = "key_hash", nullable = false, unique = true, length = 255)
    private String keyHash;

    @Column(name = "key_prefix", nullable = false, length = 20)
    private String keyPrefix;

    // Configuration group
    @Column(name = "rate_limit_per_hour", nullable = false)
    private Integer rateLimitPerHour;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "expires_at", nullable = true)
    private Instant expiresAt;

    // Audit group
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_used_at", nullable = true)
    private Instant lastUsedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Boolean isExpired() {
        if (this.expiresAt == null) {
            return false;
        } else {
            return Instant.now().isAfter(this.expiresAt);
        }
    }

    public Boolean isUsable() {
        return this.active && !isExpired();
    }

    public void updateLastUsed() {
        this.lastUsedAt = Instant.now();
    }

}
