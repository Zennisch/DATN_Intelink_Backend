package intelink.controllers;

import intelink.dto.request.LoginRequest;
import intelink.dto.request.RegisterRequest;
import intelink.dto.response.AuthResponse;
import intelink.models.User;
import intelink.models.enums.UserRole;
import intelink.security.JwtTokenProvider;
import intelink.services.UserService;
import intelink.utils.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (!userService.isUsernameAvailable(request.getUsername())) {
            return ResponseHelper.badRequest("Username already exists");
        }

        if (!userService.isEmailAvailable(request.getEmail())) {
            return ResponseHelper.badRequest("Email already exists");
        }

        User user = userService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                UserRole.USER
        );

        String token = jwtTokenProvider.generateToken(user.getUsername());

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String token = jwtTokenProvider.generateToken(authentication.getName());

        Optional<User> userOpt = userService.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", request.getUsername());
            return ResponseHelper.badRequest("User not found");
        }

        User user = userOpt.get();
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseHelper.badRequest("Invalid token format");
        }

        String token = authHeader.substring(7);
        String username = jwtTokenProvider.getUsernameFromToken(token);

        if (jwtTokenProvider.validateToken(token, username)) {
            String newToken = jwtTokenProvider.generateToken(username);
            return ResponseHelper.ok(Map.of("token", newToken));
        }

        return ResponseHelper.badRequest("Invalid token");
    }
}