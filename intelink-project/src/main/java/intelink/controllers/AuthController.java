package intelink.controllers;

import intelink.dto.object.AuthObject;
import intelink.dto.request.ForgotPasswordRequest;
import intelink.dto.request.LoginRequest;
import intelink.dto.request.RegisterRequest;
import intelink.dto.request.ResetPasswordRequest;
import intelink.dto.response.*;
import intelink.models.User;
import intelink.models.enums.UserRole;
import intelink.services.OAuthService;
import intelink.services.interfaces.IUserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final IUserService userService;
    private final OAuthService oAuthService;

    // ========== Register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) throws MessagingException {
        User user = userService.create(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                UserRole.USER
        );

        return ResponseEntity.ok(RegisterResponse.builder()
                .success(true)
                .message("Registration successful. Please check your email to verify your account.")
                .email(user.getEmail())
                .emailVerified(user.getEmailVerified())
                .build()
        );
    }

    // ========== Verify Email
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        userService.verifyEmail(token);

        return ResponseEntity.ok(VerifyEmailResponse.builder()
                .success(true)
                .message("Email verified successfully")
                .build()
        );
    }

    // ========== Forgot Password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) throws MessagingException {
        String email = forgotPasswordRequest.getEmail();
        userService.forgotPassword(email);

        return ResponseEntity.ok(ForgotPasswordResponse.builder()
                .success(true)
                .message("If the email exists, a password reset link has been sent")
                .build()
        );
    }

    // ========== Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam("token") String token,
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest
    ) {
        userService.resetPassword(token, resetPasswordRequest);

        return ResponseEntity.ok(ResetPasswordResponse.builder()
                .success(true)
                .message("Password reset successfully")
                .build()
        );
    }

    // ========== Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthObject obj = userService.login(loginRequest);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(obj.getToken())
                .refreshToken(obj.getRefreshToken())
                .username(obj.getUser().getUsername())
                .email(obj.getUser().getEmail())
                .role(obj.getUser().getRole().toString())
                .expiresAt(obj.getExpiresAt())
                .build()
        );
    }

    // ========== Refresh Token
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String authHeader) {
        AuthObject obj = userService.refreshToken(authHeader);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(obj.getToken())
                .refreshToken(obj.getRefreshToken())
                .username(obj.getUser().getUsername())
                .expiresAt(obj.getExpiresAt())
                .build()
        );
    }

    // ========== Get User Profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        User user = userService.profile(authHeader);

        return ResponseEntity.ok(UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .totalClicks(user.getTotalClicks())
                .totalShortUrls(user.getTotalShortUrls())
                .emailVerified(user.getEmailVerified())
                .authProvider(user.getAuthProvider().toString())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build()
        );
    }

    // ========== Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        userService.logout(authHeader);

        return ResponseEntity.ok(LogoutResponse.builder()
                .success(true)
                .message("Logged out successfully")
                .build()
        );
    }

    // ========== OAuth Login
    @GetMapping("/oauth/callback")
    public ResponseEntity<?> oAuthCallback(
            @RequestParam String token
    ) {
        AuthObject authObject = oAuthService.callback(token);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(authObject.getToken())
                .refreshToken(authObject.getRefreshToken())
                .username(authObject.getUser().getUsername())
                .email(authObject.getUser().getEmail())
                .role(authObject.getUser().getRole().toString())
                .expiresAt(authObject.getExpiresAt())
                .build()
        );
    }

}
