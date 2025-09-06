package intelink.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import intelink.models.enums.UserVerificationTokenType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class VerificationToken {

    // Key group
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private User user;

    // Configuration group
    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private UserVerificationTokenType type;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // Audit group
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Lifecycle hooks
    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }

    public Boolean isValid() {
        return !this.used && !Instant.now().isAfter(this.expiresAt);
    }
}
