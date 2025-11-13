package intelink.modules.oauth;

import intelink.models.OAuthAccount;
import intelink.models.enums.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    Optional<OAuthAccount> findByProviderAndProviderUserId(UserProvider provider, String providerUserId);
}
