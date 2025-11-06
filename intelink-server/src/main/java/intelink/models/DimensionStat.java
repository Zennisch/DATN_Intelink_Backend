package intelink.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import intelink.models.enums.DimensionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder

@Entity
@Table(name = "dimension_stats")
public class DimensionStat {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private ShortUrl shortUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private DimensionType type;

    @Column(name = "value", nullable = false, length = 512)
    private String value;

    @Column(name = "total_clicks", nullable = false)
    @Builder.Default
    private Long totalClicks = 0L;

    @Column(name = "allowed_clicks", nullable = false)
    @Builder.Default
    private Long allowedClicks = 0L;

    @Column(name = "blocked_clicks", nullable = false)
    @Builder.Default
    private Long blockedClicks = 0L;
}
