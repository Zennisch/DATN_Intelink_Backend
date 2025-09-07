package intelink.services.interfaces;

import intelink.dto.object.Auth;
import intelink.dto.request.auth.LoginRequest;
import intelink.dto.request.auth.RegisterRequest;
import intelink.dto.request.auth.ResetPasswordRequest;
import intelink.models.User;
import intelink.models.enums.UserRole;
import jakarta.mail.MessagingException;

import java.util.Optional;

public interface IUserService {

    User register(RegisterRequest registerRequest, UserRole role) throws MessagingException;

    void verifyEmail(String token);

    void forgotPassword(String email) throws MessagingException;

    void resetPassword(String token, ResetPasswordRequest resetPasswordRequest);

    Auth login(LoginRequest loginRequest);

    Auth refreshToken(String authHeader);

    User profile(String authHeader);

    void logout(String authHeader);

    Optional<User> findByUsername(String username);

    void incrementTotalClicks(Long userId);

    void incrementTotalShortUrls(Long userId);

    void decrementTotalShortUrls(Long userId);

    Optional<User> findByEmail(String email);

}