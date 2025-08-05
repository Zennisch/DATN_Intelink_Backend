package intelink.controllers;

import intelink.config.security.JwtTokenProvider;
import intelink.dto.request.LoginRequest;
import intelink.dto.request.RegisterRequest;
import intelink.dto.request.ValidateTokenRequest;
import intelink.dto.response.AuthResponse;
import intelink.dto.response.LogoutResponse;
import intelink.dto.response.UserProfileResponse;
import intelink.dto.response.ValidateTokenResponse;
import intelink.models.User;
import intelink.models.enums.UserRole;
import intelink.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = userService.create(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                UserRole.USER
        );

        String token = jwtTokenProvider.generateToken(user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
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

        String token = jwtTokenProvider.generateToken(authentication.getName());
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());
        Long expiresIn = jwtTokenProvider.getExpirationTimeFromToken(token);
        User user = userOpt.get();

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
