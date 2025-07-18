package intelink.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "short_code", nullable = false, unique = true)
    private String shortCode;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "password", nullable = true, length = 255)
    private String password;

    @Column(name = "description", nullable = true, length = 255)
    private String description;

    @Builder.Default
    @Column(name = "max_usage", nullable = true)
    private Long maxUsage = 0L;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "total_clicks", nullable = false)
    private Long totalClicks = 0L;

    @Column(name = "expires_at", nullable = true)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @OneToMany(mappedBy = "shortUrl", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<HourlyStat> hourlyStats = new ArrayList<>();

    @OneToMany(mappedBy = "shortUrl", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<DailyStat> dailyStats = new ArrayList<>();

    @OneToMany(mappedBy = "shortUrl", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<DimensionStat> dimensionStats = new ArrayList<>();

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

}
