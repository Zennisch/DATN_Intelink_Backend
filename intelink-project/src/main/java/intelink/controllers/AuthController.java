package intelink.controllers;

import intelink.dto.object.AuthToken;
import intelink.dto.object.SubscriptionInfo;
import intelink.dto.request.auth.ForgotPasswordRequest;
import intelink.dto.request.auth.LoginRequest;
import intelink.dto.request.auth.RegisterRequest;
import intelink.dto.request.auth.ResetPasswordRequest;
import intelink.dto.response.auth.AuthInfoResponse;
import intelink.dto.response.auth.AuthTokenResponse;
import intelink.dto.response.auth.RegisterResponse;
import intelink.dto.response.auth.UserProfileResponse;
import intelink.models.Subscription;
import intelink.models.User;
import intelink.models.enums.UserRole;
import intelink.services.interfaces.IOAuthService;
import intelink.services.interfaces.ISubscriptionService;
import intelink.services.interfaces.IUserService;
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
public class AuthController {

    private final IUserService userService;
    private final ISubscriptionService subscriptionService;
    private final IOAuthService oAuthService;

    // ========== Register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) throws MessagingException {
        User user = userService.register(request, UserRole.USER);
        String msg = "Registration successful. Please check your email to verify your account.";
        RegisterResponse resp = new RegisterResponse(true, msg, user.getEmail(), user.getEmailVerified());
        return ResponseEntity.ok(resp);
    }

    // ========== Verify Email
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        userService.verifyEmail(token);
        String msg = "Email verified successfully";
        AuthInfoResponse resp = new AuthInfoResponse(true, msg);
        return ResponseEntity.ok(resp);
    }

    // ========== Forgot Password
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) throws MessagingException {
        String email = request.getEmail();
        userService.forgotPassword(email);
        String msg = "If the email exists, a password reset link has been sent to " + email;
        AuthInfoResponse resp = new AuthInfoResponse(true, msg);
        return ResponseEntity.ok(resp);
    }

    // ========== Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam("token") String token,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        userService.resetPassword(token, request);
        String msg = "Password reset successfully. You can now log in with your new password.";
        AuthInfoResponse resp = new AuthInfoResponse(true, msg);
        return ResponseEntity.ok(resp);
    }

    // ========== Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        AuthToken obj = userService.login(request);
        AuthTokenResponse resp = AuthTokenResponse.fromEntity(obj);
        return ResponseEntity.ok(resp);
    }

    // ========== Refresh Token
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails);
        AuthToken obj = userService.refreshToken(user);
        AuthTokenResponse resp = AuthTokenResponse.fromEntity(obj);
        return ResponseEntity.ok(resp);
    }

    // ========== OAuth Login
    @GetMapping("/oauth/callback")
    public ResponseEntity<?> oAuthCallback(@RequestParam String token) {
        AuthToken obj = oAuthService.callback(token);
        AuthTokenResponse resp = AuthTokenResponse.fromEntity(obj);
        return ResponseEntity.ok(resp);
    }

    // ========== Get User Profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Subscription subscription = subscriptionService.findCurrentActiveSubscription(user);
        SubscriptionInfo subscriptionInfo = SubscriptionInfo.fromEntities(subscription, subscription.getSubscriptionPlan());
        UserProfileResponse resp = UserProfileResponse.fromEntities(user, subscriptionInfo);
        return ResponseEntity.ok(resp);
    }

    // ========== Logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails);
        userService.logout(user);
        String msg = "Logged out successfully";
        AuthInfoResponse resp = new AuthInfoResponse(true, msg);
        return ResponseEntity.ok(resp);
    }

}
