package intelink.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_stats", indexes = {
        @Index(name = "idx_short_code_date", columnList = "short_code,date"),
        @Index(name = "idx_date", columnList = "date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"hourlyStats", "shortUrl"})
@Builder
@Slf4j
public class DailyStat {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 50)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "short_code", nullable = false, length = 20)
    private String shortCode;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Builder.Default
    @Column(name = "click_count", nullable = false)
    @Min(0)
    private Long clickCount = 0L;

    @Builder.Default
    @OneToMany(mappedBy = "dailyStat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HourlyStat> hourlyStats = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_code", referencedColumnName = "short_code", insertable = false, updatable = false)
    private ShortUrl shortUrl;

    @Transient
    public Long getClicksForHour(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }

        return hourlyStats.stream()
                .filter(hs -> hs.getHour() == hour)
                .findFirst()
                .map(HourlyStat::getClickCount)
                .orElse(0L);
    }

    @Transient
    public void incrementClicksForHour(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }

        this.hourlyStats.stream()
                .filter(h -> h.getHour() == hour)
                .findFirst()
                .ifPresent(stat -> stat.setClickCount(stat.getClickCount() + 1));
        this.clickCount++;
    }
}