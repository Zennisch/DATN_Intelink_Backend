package intelink.modules.user;

import intelink.models.User;
import intelink.models.VerificationToken;
import intelink.models.enums.VerificationTokenType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public VerificationToken createToken(User user, VerificationTokenType tokenType, Integer lifetimeInHours) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = Instant.now().plusSeconds(lifetimeInHours * 3600L);
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .type(tokenType)
                .expiresAt(expiresAt)
                .user(user)
                .build();
        verificationTokenRepository.save(verificationToken);
        return verificationToken;
    }

    public Optional<VerificationToken> findValidToken(String token, VerificationTokenType tokenType) {
        Instant now = Instant.now();
        return verificationTokenRepository.findByTokenAndTypeAndExpiresAtAfter(token, tokenType, now)
                .filter(verificationToken -> verificationToken.getExpiresAt().isAfter(now));
    }

    public void setTokenAsUsed(VerificationToken verificationToken) {
        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
    }

}
