package intelink.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "hourly_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class HourlyStat {

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
