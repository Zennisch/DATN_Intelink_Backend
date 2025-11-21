package intelink.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import intelink.models.enums.ClickStatus;
import intelink.models.enums.IPVersion;
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
@Table(name = "click_logs")
public class ClickLog {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private ShortUrl shortUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "ip_version", nullable = false)
    private IPVersion ipVersion;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 2048)
    private String userAgent;

    @Column(name = "referrer", length = 2048)
    private String referrer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClickStatus status;

    @Column(name = "reason", length = 1024)
    private String reason;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}
