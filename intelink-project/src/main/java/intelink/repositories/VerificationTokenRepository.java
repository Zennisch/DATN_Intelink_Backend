package intelink.repositories;

import intelink.models.VerificationToken;
import intelink.models.enums.VerificationTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByTokenAndTypeAndExpiresAtAfter(String token, VerificationTokenType type, Instant expiresAtAfter);
}
