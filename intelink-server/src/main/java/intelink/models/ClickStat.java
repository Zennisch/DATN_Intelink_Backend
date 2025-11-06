package intelink.models;

import intelink.models.enums.Granularity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder

@Entity
@Table(name = "click_stats")
public class ClickStat {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    public ShortUrl shortUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "granularity", nullable = false)
    public Granularity granularity;

    @Column(name = "bucket_start", nullable = false)
    public Instant bucketStart;

    @Column(name = "bucket_end", nullable = false)
    public Instant bucketEnd;

    @Column(name = "total_clicks", nullable = false)
    @Builder.Default
    public Long totalClicks = 0L;

    @Column(name = "allowed_clicks", nullable = false)
    @Builder.Default
    public Long allowedClicks = 0L;

    @Column(name = "blocked_clicks", nullable = false)
    @Builder.Default
    public Long blockedClicks = 0L;
}
