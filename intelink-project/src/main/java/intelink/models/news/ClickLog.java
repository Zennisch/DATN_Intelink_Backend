package intelink.models.news;

import com.fasterxml.jackson.annotation.JsonIgnore;
import intelink.models.news.enums.IpVersion;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "click_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class ClickLog {

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

    // Attribute group
    @Enumerated(EnumType.STRING)
    @Column(name = "ip_version", nullable = false)
    private IpVersion ipVersion;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "user_agent", nullable = true, columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "referrer", nullable = true, columnDefinition = "TEXT")
    private String referrer;

    // Audit group
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @PrePersist
    private void onCreate() {
        this.timestamp = Instant.now();
    }

}
