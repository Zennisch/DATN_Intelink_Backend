package intelink.models;

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
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    public ShortUrl shortUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "ip_version", nullable = false)
    public IPVersion ipVersion;

    @Column(name = "ip_address", nullable = false, length = 45)
    public String ipAddress;

    @Column(name = "user_agent", length = 2048)
    public String userAgent;

    @Column(name = "referrer", length = 2048)
    public String referrer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public ClickStatus status;

    @Column(name = "timestamp", nullable = false)
    public Instant timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}
