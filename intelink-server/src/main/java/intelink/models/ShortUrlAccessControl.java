package intelink.models;

import intelink.models.enums.AccessControlType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder

@Entity
@Table(name = "short_url_access_controls")
public class ShortUrlAccessControl {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    private ShortUrl shortUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AccessControlType type;

    @Column(name = "value", nullable = false, length = 2048)
    private String value;
}
