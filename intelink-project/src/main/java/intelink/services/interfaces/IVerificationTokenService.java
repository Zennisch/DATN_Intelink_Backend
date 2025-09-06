package intelink.services.interfaces;

import intelink.models.User;
import intelink.models.VerificationToken;
import intelink.models.enums.VerificationTokenType;

import java.util.Optional;

public interface IVerificationTokenService {

    VerificationToken create(User user, VerificationTokenType tokenType, Integer lifetimeInHours);

    Optional<VerificationToken> findValidToken(String token, VerificationTokenType tokenType);

    void markTokenAsUsed(VerificationToken verificationToken);

}
