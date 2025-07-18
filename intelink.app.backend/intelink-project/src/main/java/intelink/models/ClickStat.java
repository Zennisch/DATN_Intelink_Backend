package intelink.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "hourly_stats", indexes = {
    @Index(name = "idx_hourly_stats_short_url", columnList = "short_url_id"),
    @Index(name = "idx_hourly_stats_timestamp", columnList = "timestamp"),
    @Index(name = "idx_hourly_stats_short_url_timestamp", columnList = "short_url_id,timestamp")
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

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Builder.Default
    @Column(name = "total_clicks", nullable = false)
    private Long totalClicks = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    @ToString.Exclude
    private ShortUrl shortUrl;

}
