package intelink.services.interfaces;

import intelink.models.User;
import intelink.models.VerificationToken;
import intelink.models.enums.UserVerificationTokenType;

import java.util.Optional;

public interface IVerificationTokenService {

    VerificationToken create(User user, UserVerificationTokenType tokenType, Integer lifetimeInHours);

    Optional<VerificationToken> findValidToken(String token, UserVerificationTokenType tokenType);

    void markTokenAsUsed(VerificationToken verificationToken);

}
