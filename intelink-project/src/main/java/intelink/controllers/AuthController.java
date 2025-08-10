package intelink.controllers;

import intelink.config.security.JwtTokenProvider;
import intelink.dto.object.LoginObject;
import intelink.dto.request.*;
import intelink.dto.response.*;
import intelink.models.User;
import intelink.models.enums.UserRole;
import intelink.services.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    private final JwtTokenProvider jwtTokenProvider;

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

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginObject obj = userService.login(loginRequest);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(obj.getToken())
                .refreshToken(obj.getRefreshToken())
                .username(obj.getUser().getUsername())
                .email(obj.getUser().getEmail())
                .role(obj.getUser().getRole().toString())
                .expiresIn(obj.getExpiresIn())
                .build()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Invalid token format");
        }

        String oldToken = authHeader.substring(7);
        String username = jwtTokenProvider.getUsernameFromToken(oldToken);

        if (!jwtTokenProvider.validateToken(oldToken, username)) {
            throw new BadCredentialsException("Invalid or expired token");
        }

        String token = jwtTokenProvider.generateToken(username);
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);
        Long expiresIn = jwtTokenProvider.getExpirationTimeFromToken(token);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .username(username)
                .expiresIn(expiresIn)
                .build()
        );
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Invalid token format");
        }

        String token = authHeader.substring(7);
        String username = jwtTokenProvider.getUsernameFromToken(token);

        if (!jwtTokenProvider.validateToken(token, username)) {
            throw new BadCredentialsException("Invalid or expired token");
        }

        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", username);
            throw new BadCredentialsException("User not found");
        }

        User user = userOpt.get();
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtTokenProvider.getUsernameFromToken(token);

                if (jwtTokenProvider.validateToken(token, username)) {
                    SecurityContextHolder.clearContext();
                    log.info("User logged out successfully: {}", username);
                }
            }

            return ResponseEntity.ok(LogoutResponse.builder()
                    .success(true)
                    .message("Logged out successfully")
                    .build()
            );
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            return ResponseEntity.ok(LogoutResponse.builder()
                    .success(true)
                    .message("Logged out successfully")
                    .build()
            );
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@Valid @RequestBody ValidateTokenRequest validateRequest) {
        try {
            String token = validateRequest.getToken();
            String username = jwtTokenProvider.getUsernameFromToken(token);

            if (jwtTokenProvider.validateToken(token, username)) {
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    Long expiresIn = jwtTokenProvider.getExpirationTimeFromToken(token);

                    return ResponseEntity.ok(ValidateTokenResponse.builder()
                            .valid(true)
                            .username(user.getUsername())
                            .role(user.getRole().toString())
                            .expiresIn(expiresIn)
                            .message("Token is valid")
                            .build()
                    );
                }
            }

            return ResponseEntity.ok(ValidateTokenResponse.builder()
                    .valid(false)
                    .message("Invalid or expired token")
                    .build()
            );
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return ResponseEntity.ok(ValidateTokenResponse.builder()
                    .valid(false)
                    .message("Invalid token format")
                    .build()
            );
        }
    }

}
