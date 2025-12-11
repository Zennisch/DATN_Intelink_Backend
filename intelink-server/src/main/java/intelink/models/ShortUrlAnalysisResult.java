package intelink.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import intelink.models.enums.ShortUrlAnalysisEngine;
import intelink.models.enums.ShortUrlAnalysisStatus;
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
    @JsonIgnore
    private ShortUrl shortUrl;

    // Status group
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShortUrlAnalysisStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "engine", nullable = false)
    private ShortUrlAnalysisEngine engine;

    @Column(name = "threat_type", nullable = false)
    private String threatType;

    @Column(name = "platform_type", nullable = false)
    private String platformType;

    @Column(name = "cache_duration", nullable = true)
    private String cacheDuration;

    @Column(name = "details", nullable = true, columnDefinition = "TEXT")
    private String details;

    // Audit group
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Lifecycle hooks
    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }
}
