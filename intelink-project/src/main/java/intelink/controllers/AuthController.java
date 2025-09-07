package intelink.controllers;

import intelink.dto.object.Auth;
import intelink.dto.request.auth.ForgotPasswordRequest;
import intelink.dto.request.auth.LoginRequest;
import intelink.dto.request.auth.RegisterRequest;
import intelink.dto.request.auth.ResetPasswordRequest;
import intelink.dto.response.auth.*;
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
        User user = userService.register(registerRequest, UserRole.USER);

        String msg = "Registration successful. Please check your email to verify your account.";
        return ResponseEntity.ok(new RegisterResponse(true, msg, user.getEmail(), user.getEmailVerified()));
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

        String msg = "If the email exists, a password reset link has been sent to " + email;
        return ResponseEntity.ok(new ForgotPasswordResponse(true, msg));
    }

    // ========== Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam("token") String token,
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest
    ) {
        userService.resetPassword(token, resetPasswordRequest);

        String msg = "Password reset successfully. You can now log in with your new password.";
        return ResponseEntity.ok(new ForgotPasswordResponse(true, msg));
    }

    // ========== Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        Auth obj = userService.login(loginRequest);

        AuthResponse resp = AuthResponse.fromEntity(obj);
        return ResponseEntity.ok(resp);
    }

    // ========== Refresh Token
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String authHeader) {
        Auth obj = userService.refreshToken(authHeader);

        AuthResponse resp = AuthResponse.fromEntity(obj);
        return ResponseEntity.ok(resp);
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
        Auth auth = oAuthService.callback(token);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(auth.getToken())
                .refreshToken(auth.getRefreshToken())
                .username(auth.getUser().getUsername())
                .email(auth.getUser().getEmail())
                .role(auth.getUser().getRole().toString())
                .expiresAt(auth.getExpiresAt())
                .build()
        );
    }

}
