package intelink.modules.auth.controllers;

import intelink.dto.auth.*;
import intelink.models.User;
import intelink.models.enums.UserRole;
import intelink.modules.auth.services.AuthService;
import intelink.modules.auth.services.OAuthAccountService;
import intelink.utils.helper.AuthToken;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    private final AuthService authService;
    private final OAuthAccountService oAuthAccountService;

    // ========== Register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) throws MessagingException {
        User user = authService.register(request, UserRole.USER);
        String msg = "Registration successful. Please check your email to verify your account.";
        RegisterResponse resp = new RegisterResponse(true, msg, user.getEmail(), user.getVerified());
        return ResponseEntity.ok(resp);
    }

    // ========== Verify Email
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        String msg = "Email verified successfully";
        AuthInfoResponse resp = new AuthInfoResponse(true, msg);
        return ResponseEntity.ok(resp);
    }

    // ========== Forgot Password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) throws MessagingException {
        String email = request.email();
        authService.forgotPassword(email);
        String msg = "The password reset link has been sent to " + email;
        AuthInfoResponse resp = new AuthInfoResponse(true, msg);
        return ResponseEntity.ok(resp);
    }

    // ========== Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam("token") String token,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(token, request);
        String msg = "Password reset successfully. You can now log in with your new password.";
        AuthInfoResponse resp = new AuthInfoResponse(true, msg);
        return ResponseEntity.ok(resp);
    }

    // ========== Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        AuthToken obj = authService.login(request);
        AuthTokenResponse resp = AuthTokenResponse.fromEntity(obj);
        return ResponseEntity.ok(resp);
    }

    // ========== Refresh Token
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails);
        AuthToken obj = authService.refreshToken(user);
        AuthTokenResponse resp = AuthTokenResponse.fromEntity(obj);
        return ResponseEntity.ok(resp);
    }

    // ========== Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails);
        authService.logout(user);
        String msg = "Logged out successfully";
        AuthInfoResponse resp = new AuthInfoResponse(true, msg);
        return ResponseEntity.ok(resp);
    }

    // ========== Get User Profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getCurrentUser(userDetails);
        UserProfileResponse resp = authService.getProfile(user);
        return ResponseEntity.ok(resp);
    }

    // ========== OAuth Login
    @GetMapping("/oauth/callback")
    public ResponseEntity<?> oAuthCallback(@RequestParam String token) {
        AuthToken obj = oAuthAccountService.callback(token);
        AuthTokenResponse resp = AuthTokenResponse.fromEntity(obj);
        return ResponseEntity.ok(resp);
    }
}
