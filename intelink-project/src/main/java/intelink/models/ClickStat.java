package intelink.models;

import intelink.models.enums.Granularity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "click_stats", indexes = {
        @Index(name = "idx_hourly_stats_short_url", columnList = "short_url_id"),
        @Index(name = "idx_hourly_stats_bucket", columnList = "bucket"),
        @Index(name = "idx_hourly_stats_short_url_bucket", columnList = "short_url_id,bucket")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class ClickStat {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "granularity", nullable = false)
    private Granularity granularity;

    @Column(name = "bucket", nullable = false)
    private Instant bucket;

    @Builder.Default
    @Column(name = "total_clicks", nullable = false)
    private Long totalClicks = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    @ToString.Exclude
    private ShortUrl shortUrl;

}
