package intelink.models.news;

import intelink.models.news.enums.UserProvider;
import intelink.models.news.enums.UserRole;
import intelink.models.news.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username"),
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_role", columnList = "role"),
        @Index(name = "idx_users_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"passwordHash"})
@Builder
public class User {

    // Key group
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // Auth group
    @Size(min = 3, max = 16)
    @Column(name = "username", nullable = false, unique = true, length = 16)
    private String username;

    @Email
    @Column(name = "email", nullable = false, unique = true, length = 128)
    private String email;

    @Column(name = "password_hash", nullable = true, length = 255)
    private String passwordHash;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "last_login_at", nullable = true)
    private Instant lastLoginAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 8)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 16)
    @Builder.Default
    private UserProvider provider = UserProvider.LOCAL;

    @Column(name = "provider_user_id", nullable = true, length = 128)
    private String providerUserId;

    // Information group
    @Column(name = "display_name", nullable = true, length = 64)
    private String displayName;

    @Column(name = "bio", nullable = true, length = 512)
    private String bio;

    @Column(name = "profile_picture_url", nullable = true, length = 512)
    private String profilePictureUrl;

    // Statistics group
    @Column(name = "total_short_urls", nullable = false)
    @Builder.Default
    private Integer totalShortUrls = 0;

    @Column(name = "total_clicks", nullable = false)
    @Builder.Default
    private Long totalClicks = 0L;

    // Configuration group
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

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
