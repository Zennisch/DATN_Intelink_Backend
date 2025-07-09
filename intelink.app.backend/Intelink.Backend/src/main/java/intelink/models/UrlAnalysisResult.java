package intelink.models;

import intelink.models.enums.AnalysisStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "url_analysis_results", indexes = {
        @Index(name = "idx_short_code", columnList = "short_code"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_analyzed_at", columnList = "analyzed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class UrlAnalysisResult {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "short_code", nullable = false, length = 20)
    private String shortCode;

    @Enumerated
    @Column(name = "status", nullable = false, length = 20)
    private AnalysisStatus status;

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;

    @Column(name = "analysis_engine", nullable = false, length = 100)
    private String analysisEngine;

    @Column(name = "details", nullable = true, columnDefinition = "TEXT")
    private String details;

    @Column(name = "analyzed_at", nullable = false)
    private Instant analyzedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_code", referencedColumnName = "short_code", insertable = false, updatable = false)
    private ShortUrl shortUrl;

    @PrePersist
    private void onCreate() {
        if (this.analyzedAt == null) {
            this.analyzedAt = Instant.now();
        }
    }
}