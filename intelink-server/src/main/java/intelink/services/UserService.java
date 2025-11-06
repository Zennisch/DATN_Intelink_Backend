package intelink.services;

import intelink.dto.auth.RegisterRequest;
import intelink.models.User;
import intelink.models.VerificationToken;
import intelink.models.enums.UserRole;
import intelink.models.enums.VerificationTokenType;
import intelink.repositories.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.url.verify-email}")
    private String verificationEmailUrlTemplate;

    @Value("${app.url.reset-password}")
    private String resetPasswordEmailUrlTemplate;

    private void sendVerificationEmail(User user) throws MessagingException {
        VerificationToken verificationToken = verificationTokenService.createToken(user, VerificationTokenType.EMAIL_VERIFICATION, 24);
        String verificationLink = verificationEmailUrlTemplate.replace("{token}", verificationToken.getToken());
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);
        log.info("[UserService] Verification email sent to {}", user.getEmail());
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupUnverifiedUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        int deletedCount = userRepository.deleteUnverifiedUsersBefore(threshold);
        log.info("[UserService] Cleaned up {} unverified users", deletedCount);
    }

    @Transactional
    public User register(RegisterRequest registerRequest, UserRole role) throws MessagingException {
        // 0. Extract fields
        String username = registerRequest.username();
        String email = registerRequest.email();
        String password = registerRequest.password();

        // 1. Validate input
        if (userRepository.existsByUsernameAndVerifiedTrue(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmailAndVerifiedTrue(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // 2. Check unverified users
        Optional<User> existingUserByEmail = userRepository.findByEmailAndVerifiedFalse(email);
        Optional<User> existingUserByUsername = userRepository.findByUsernameAndVerifiedFalse(username);

        // Scenario: Both email AND username match same unverified user -> Resend verification
        if (existingUserByEmail.isPresent() && existingUserByUsername.isPresent()
                && existingUserByEmail.get().getId().equals(existingUserByUsername.get().getId())) {
            User user = existingUserByEmail.get();
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            sendVerificationEmail(user);
            return user;
        }

        // Scenario: Email hoặc username bị chiếm bởi user chưa verify -> Từ chối
        if (existingUserByEmail.isPresent()) {
            throw new IllegalArgumentException("Email is pending verification. Please verify or contact support.");
        }
        if (existingUserByUsername.isPresent()) {
            throw new IllegalArgumentException("Username is pending verification. Please choose another username.");
        }

        // 3. Scenario: No matches -> Create new user
        User user = User.builder().username(username).email(email).password(passwordEncoder.encode(password)).role(role).build();

        User savedUser = userRepository.save(user);
        log.info("[UserService] User created with ID: {}", savedUser.getId());

        // 3. Create FREE subscription for new user

        // 4. Generate email verification token and send email
        sendVerificationEmail(user);

        return savedUser;
    }

    @Transactional
    public void verifyEmail(String token) {
        // 1. Validate token
        Optional<VerificationToken> tokenOpt = verificationTokenService.findValidToken(token, VerificationTokenType.EMAIL_VERIFICATION);
        if (tokenOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid or expired verification token");
        }

        // 2. Check if token already used
        if (tokenOpt.get().getUsed()) {
            throw new BadCredentialsException("Verification token has already been used");
        }

        // 3. Set token as used
        VerificationToken verificationToken = tokenOpt.get();
        User user = verificationToken.getUser();
        verificationTokenService.setTokenAsUsed(verificationToken);
        log.info("[UserService] Verification token marked as used for user ID: {}", user.getId());

        // 4. Mark user's email as verified
        user.setVerified(true);
        userRepository.save(user);
        log.info("[UserService] Email verified for user ID: {}", user.getId());
    }

    @Transactional
    public void forgotPassword(String email) throws MessagingException {
        // 1. Check if user with the email exists
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Do not reveal if the email exists for security reasons
            log.debug("[UserService] Password reset requested for non-existing email: {}", email);
            return;
        }
        log.debug("[UserService] Password reset requested for email: {}", email);

        // 2. If exists, generate password reset token
        User user = userOpt.get();
        VerificationToken verificationToken = verificationTokenService.createToken(user, VerificationTokenType.PASSWORD_RESET, 1);
        String resetLink = resetPasswordEmailUrlTemplate.replace("{token}", verificationToken.getToken());

        // 3. Send password reset email
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);
        log.info("[UserService] Password reset email sent to {}", user.getEmail());
    }
}
