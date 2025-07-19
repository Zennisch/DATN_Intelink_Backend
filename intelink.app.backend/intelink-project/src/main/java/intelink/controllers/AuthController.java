package intelink.controllers;

import intelink.dto.request.RegisterRequest;
import intelink.dto.response.AuthResponse;
import intelink.models.enums.UserRole;
import intelink.security.JwtTokenProvider;
import intelink.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            var user = userService.create(
                    registerRequest.getUsername(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    UserRole.USER
            );

            String token = jwtTokenProvider.generateToken(user.getUsername());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
            Long expirationTime = jwtTokenProvider.getExpirationTimeFromToken(token);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwtTokenProvider.generateToken(user.getUsername()))
                    .refreshToken(jwtTokenProvider.generateRefreshToken(user.getUsername()))
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().toString())
                    .expiresIn(jwtTokenProvider.getExpirationTimeFromToken(token))
                    .build()
            );
        } catch (Exception e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
