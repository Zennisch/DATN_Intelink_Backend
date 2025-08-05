package intelink.models;

import intelink.models.enums.AnalysisStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analysis_results", indexes = {
        @Index(name = "idx_analysis_results_short_url", columnList = "short_url_id"),
        @Index(name = "idx_analysis_results_status", columnList = "status"),
        @Index(name = "idx_analysis_results_analyzed_at", columnList = "analyzed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class AnalysisResult {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Enumerated
    @Column(name = "status", nullable = false)
    private AnalysisStatus status;

    @Column(name = "analysis_engine", nullable = false)
    private String analysisEngine;

    @Column(name = "threat_type", nullable = false)
    private String threatType;

    @Column(name = "platform_type", nullable = false)
    private String platformType;

    @Column(name = "cache_duration", nullable = true)
    private String cacheDuration;

    @Column(name = "details", nullable = true, columnDefinition = "TEXT")
    private String details;

    @Column(name = "analyzed_at", nullable = false)
    private Instant analyzedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    @ToString.Exclude
    private ShortUrl shortUrl;

    @PrePersist
    private void onCreate() {
        if (this.analyzedAt == null) {
            this.analyzedAt = Instant.now();
        }
    }

}
