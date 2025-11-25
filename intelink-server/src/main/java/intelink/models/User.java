package intelink.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import intelink.models.enums.UserRole;
import intelink.models.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<OAuthAccount> oauthAccounts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<VerificationToken> verificationTokens;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<ShortUrl> shortUrls;

    @Size(min = 6, max = 32)
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Email
    @Column(name = "email", nullable = false, unique = true, length = 128)
    private String email;

    @Column(name = "password")
    @ToString.Exclude
    private String password;

    @Column(name = "verified", nullable = false)
    @Builder.Default
    private Boolean verified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "profile_name", length = 64)
    private String profileName;

    @Column(name = "profile_picture_url", length = 256)
    private String profilePictureUrl;

    @Column(name = "total_short_urls", nullable = false)
    @Builder.Default
    private Integer totalShortUrls = 0;

    @Column(name = "total_clicks", nullable = false)
    @Builder.Default
    private Long totalClicks = 0L;

    @Column(name = "balance", nullable = false)
    @Builder.Default
    private Double balance = 0.0;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
