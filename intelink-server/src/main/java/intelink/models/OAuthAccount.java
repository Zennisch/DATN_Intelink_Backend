package intelink.models;

import intelink.models.enums.UserProvider;
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
public class OAuthAccount {
    public UUID id;
    public User user;
    
    public UserProvider provider;
    public String providerUserId;
    public String providerUsername;
    public String providerEmail;
    
    public String accessToken;
    public String refreshToken;
    public Instant tokenExpiresAt;
    
    public Instant createdAt;
    public Instant updatedAt;
}
