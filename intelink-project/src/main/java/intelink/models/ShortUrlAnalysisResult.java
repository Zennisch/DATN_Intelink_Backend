package intelink.models;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import intelink.models.enums.ShortUrlAnalysisEngine;
import intelink.models.enums.ShortUrlAnalysisStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "threat_type", nullable = false)
    private String threatType;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform_type", nullable = false)
    private String platformType;

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
