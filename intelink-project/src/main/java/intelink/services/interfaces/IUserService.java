package intelink.services.interfaces;

import intelink.dto.object.AuthToken;
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

    AuthToken login(LoginRequest loginRequest);

    AuthToken refreshToken(String authHeader);

    User profile(String authHeader);

    void logout(String authHeader);

    Optional<User> findByUsername(String username);

    void increaseTotalClicks(Long userId);

    void increaseTotalShortUrls(Long userId);

    void decreaseTotalShortUrls(Long userId);

}