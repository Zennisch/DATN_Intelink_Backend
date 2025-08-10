package intelink.controllers;

import intelink.config.security.JwtTokenProvider;
import intelink.dto.request.LoginRequest;
import intelink.dto.request.RegisterRequest;
import intelink.dto.request.ValidateTokenRequest;
import intelink.dto.response.*;
import intelink.models.User;
import intelink.models.VerificationToken;
import intelink.models.enums.OAuthProvider;
import intelink.models.enums.TokenType;
import intelink.models.enums.UserRole;
import intelink.services.EmailService;
import intelink.services.UserService;
import intelink.services.VerificationTokenService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Value("${app.url.verify-email}")
    private String verificationEmailUrlTemplate;

    // ========== Register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) throws MessagingException {
        User user = userService.create(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                UserRole.USER
        );

        VerificationToken verificationToken = verificationTokenService.create(user, TokenType.EMAIL_VERIFICATION, 24);
        String verificationLink = verificationEmailUrlTemplate.replace("{token}", verificationToken.getToken());
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);

        return ResponseEntity.ok(RegisterResponse.builder()
                .success(true)
                .message("Registration successful. Please check your email to verify your account.")
                .email(user.getEmail())
                .emailVerified(user.getEmailVerified())
                .build()
        );
    }

    // ========== Verify Email
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        Optional<VerificationToken> tokenOpt = verificationTokenService.findValidToken(token, TokenType.EMAIL_VERIFICATION);
        if (tokenOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid or expired verification token");
        }

        VerificationToken verificationToken = tokenOpt.get();
        userService.updateEmailVerified(verificationToken.getUser().getId(), true);
        verificationTokenService.markTokenAsUsed(verificationToken);

        return ResponseEntity.ok(VerifyEmailResponse.builder()
                .success(true)
                .message("Email verified successfully")
                .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        Optional<User> userOpt = userService.findByUsername(loginRequest.getUsername());
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", loginRequest.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userOpt.get();

        // Kiểm tra email verification (chỉ cho LOCAL auth provider)
        if (user.getAuthProvider() == OAuthProvider.LOCAL && !user.getEmailVerified()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Please verify your email before logging in",
                    "emailVerified", false
            ));
        }

        // Cập nhật last login
        userService.updateLastLogin(user.getUsername());

        String token = jwtTokenProvider.generateToken(authentication.getName());
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());
        Long expiresIn = jwtTokenProvider.getExpirationTimeFromToken(token);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .expiresIn(expiresIn)
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

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody Map<String, String> request) {
        String token = request.get("token");

        // Cần inject VerificationTokenService
        // Optional<VerificationToken> tokenOpt = verificationTokenService.findValidToken(token, TokenType.EMAIL_VERIFICATION);

        // if (tokenOpt.isEmpty()) {
        //     return ResponseEntity.badRequest().body(Map.of(
        //             "success", false,
        //             "message", "Invalid or expired verification token"
        //     ));
        // }

        // VerificationToken verificationToken = tokenOpt.get();
        // userService.updateEmailVerified(verificationToken.getUser().getId(), true);
        // verificationTokenService.markAsUsed(token);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email verified successfully"
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody Map<String, String> request) {
        String email = request.get("email");

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Không tiết lộ thông tin user có tồn tại hay không
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "If the email exists, a password reset link has been sent"
            ));
        }

        // User user = userOpt.get();
        // VerificationToken token = verificationTokenService.createToken(user, TokenType.PASSWORD_RESET, 1); // 1 hour
        // emailService.sendPasswordResetEmail(user.getEmail(), token.getToken());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "If the email exists, a password reset link has been sent"
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        // Cần inject VerificationTokenService
        // Optional<VerificationToken> tokenOpt = verificationTokenService.findValidToken(token, TokenType.PASSWORD_RESET);

        // if (tokenOpt.isEmpty()) {
        //     return ResponseEntity.badRequest().body(Map.of(
        //             "success", false,
        //             "message", "Invalid or expired reset token"
        //     ));
        // }

        // VerificationToken verificationToken = tokenOpt.get();
        // userService.changePassword(verificationToken.getUser().getUsername(), newPassword);
        // verificationTokenService.markAsUsed(token);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password reset successfully"
        ));
    }
}
