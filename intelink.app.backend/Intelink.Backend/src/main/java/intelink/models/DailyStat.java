package intelink.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import intelink.utils.JsonUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Entity
@Table(name = "daily_stats", indexes = {
        @Index(name = "idx_short_code", columnList = "short_code"),
        @Index(name = "idx_date", columnList = "date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
@Slf4j
public class DailyStat {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 50)
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "short_code", nullable = false, length = 20)
    private String shortCode;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Builder.Default
    @Column(name = "click_count", nullable = false)
    @Min(0)
    private Long clickCount = 0L;

    @Column(name = "hourly_clicks", nullable = true, columnDefinition = "TEXT")
    private String hourlyClicksJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_code", referencedColumnName = "short_code", insertable = false, updatable = false)
    private ShortUrl shortUrl;

    @PrePersist
    private void onCreate() {
        if (this.id == null) {
            this.id = this.shortCode + "_" + this.date.toString();
        }
    }

    @Transient
    public int[] getHourlyClicks() {
        if (hourlyClicksJson == null || hourlyClicksJson.isEmpty()) {
            return new int[24];
        }

        try {
            int[] result = JsonUtils.fromJson(hourlyClicksJson, int[].class);
            return result.length == 24 ? result : padArray(result);
        } catch (RuntimeException e) {
            log.warn("Failed to parse hourly clicks JSON for DailyStat id: {}", id, e);
            return new int[24];
        }

    }

    @Transient
    public void setHourlyClicks(int[] hourlyClicks) {
        if (hourlyClicks == null || hourlyClicks.length != 24) {
            hourlyClicks = padArray(hourlyClicks);
        }

        try {
            this.hourlyClicksJson = JsonUtils.toJson(hourlyClicks);
        } catch (RuntimeJsonMappingException e) {
            log.error("Failed to serialize hourly clicks for DailyStat id: {}", id, e);
            this.hourlyClicksJson = "[" + "0,".repeat(23) + "0]";
        }
    }

    private int[] padArray(int[] array) {
        int[] result = new int[24];
        if (array != null) {
            System.arraycopy(array, 0, result, 0, Math.min(array.length, 24));
        }
        return result;
    }
}