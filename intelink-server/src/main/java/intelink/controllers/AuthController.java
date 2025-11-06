package intelink.controllers;

import intelink.dto.auth.AuthInfoResponse;
import intelink.dto.auth.ForgotPasswordRequest;
import intelink.dto.auth.RegisterRequest;
import intelink.dto.auth.RegisterResponse;
import intelink.models.User;
import intelink.models.enums.UserRole;
import intelink.services.OAuthAccountService;
import intelink.services.UserService;
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

    private final UserService userService;
    private final OAuthAccountService oAuthAccountService;

    // ========== Register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) throws MessagingException {
        User user = userService.register(request, UserRole.USER);
        String msg = "Registration successful. Please check your email to verify your account.";
        RegisterResponse resp = new RegisterResponse(true, msg, user.getEmail(), user.getVerified());
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
        String email = request.email();
        userService.forgotPassword(email);
        String msg = "The password reset link has been sent to " + email;
        AuthInfoResponse resp = new AuthInfoResponse(true, msg);
        return ResponseEntity.ok(resp);
    }
}
