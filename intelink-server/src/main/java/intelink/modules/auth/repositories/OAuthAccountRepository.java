package intelink.modules.auth.repositories;

import intelink.models.OAuthAccount;
import intelink.models.enums.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    @Query("SELECT o FROM OAuthAccount o JOIN FETCH o.user WHERE o.provider = :provider AND o.providerUserId = :providerUserId")
    Optional<OAuthAccount> findByProviderAndProviderUserId(@Param("provider") UserProvider provider, @Param("providerUserId") String providerUserId);
}
