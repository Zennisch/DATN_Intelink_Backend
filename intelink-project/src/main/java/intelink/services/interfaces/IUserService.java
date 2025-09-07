package intelink.services.interfaces;

import intelink.dto.object.AuthObject;
import intelink.dto.request.LoginRequest;
import intelink.dto.request.ResetPasswordRequest;
import intelink.models.User;
import intelink.models.enums.UserRole;
import jakarta.mail.MessagingException;

import java.util.Optional;

public interface IUserService {

    User register(String username, String email, String password, UserRole role) throws MessagingException;

    void verifyEmail(String token);

    void forgotPassword(String email) throws MessagingException;

    void resetPassword(String token, ResetPasswordRequest resetPasswordRequest);

    AuthObject login(LoginRequest loginRequest);

    AuthObject refreshToken(String authHeader);

    User profile(String authHeader);

    void logout(String authHeader);

    Optional<User> findByUsername(String username);

    void incrementTotalClicks(Long userId);

    void incrementTotalShortUrls(Long userId);

    void decrementTotalShortUrls(Long userId);

    Optional<User> findByEmail(String email);

}