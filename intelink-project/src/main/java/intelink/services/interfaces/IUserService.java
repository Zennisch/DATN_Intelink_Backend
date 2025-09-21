package intelink.services.interfaces;

import intelink.dto.object.AuthToken;
import intelink.dto.request.auth.LoginRequest;
import intelink.dto.request.auth.RegisterRequest;
import intelink.dto.request.auth.ResetPasswordRequest;
import intelink.models.User;
import intelink.models.enums.UserRole;
import jakarta.mail.MessagingException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface IUserService {

    User register(RegisterRequest registerRequest, UserRole role) throws MessagingException;

    void verifyEmail(String token);

    void forgotPassword(String email) throws MessagingException;

    void resetPassword(String token, ResetPasswordRequest resetPasswordRequest);

    AuthToken login(LoginRequest loginRequest);

    AuthToken refreshToken(User user);

    void logout(User user);

    Optional<User> findByUsername(String username);

    void increaseTotalClicks(Long userId);

    void increaseTotalShortUrls(Long userId);

    void decreaseTotalShortUrls(Long userId);

    User getCurrentUser();

    User getCurrentUser(UserDetails userDetails);

}