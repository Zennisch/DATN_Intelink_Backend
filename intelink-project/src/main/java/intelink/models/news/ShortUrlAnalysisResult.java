package intelink.models.news;

import intelink.models.news.enums.ShortUrlAnalysisEngine;
import intelink.models.news.enums.ShortUrlAnalysisPlatformType;
import intelink.models.news.enums.ShortUrlAnalysisStatus;
import intelink.models.news.enums.ShortUrlAnalysisThreatType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "short_url_analysis_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class ShortUrlAnalysisResult {

    // Key group
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    @ToString.Exclude
    private ShortUrl shortUrl;

    // Status group
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShortUrlAnalysisStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "engine", nullable = false)
    private ShortUrlAnalysisEngine engine;

    @Enumerated(EnumType.STRING)
    @Column(name = "threat_type", nullable = false)
    private ShortUrlAnalysisThreatType threatType;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform_type", nullable = false)
    private ShortUrlAnalysisPlatformType platformType;

    @Column(name = "cache_duration", nullable = true)
    private String cacheDuration;

    @Column(name = "details", nullable = true, columnDefinition = "TEXT")
    private String details;

    // Audit group
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.Instant createdAt;

    // Lifecycle hooks
    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }
}
