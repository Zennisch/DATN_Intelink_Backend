package intelink.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "hourly_stats", indexes = {
        @Index(name = "idx_daily_stat_hour", columnList = "daily_stat_id,hour")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "dailyStat")
@Builder
public class HourlyStat {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "hour", nullable = false)
    @Min(0)
    @Max(23)
    private Integer hour;

    @Builder.Default
    @Column(name = "click_count", nullable = false)
    @Min(0)
    private Long clickCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_stat_id", nullable = false)
    private DailyStat dailyStat;
}