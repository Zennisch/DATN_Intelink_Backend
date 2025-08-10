package intelink.services;

import intelink.models.User;
import intelink.models.VerificationToken;
import intelink.models.enums.OAuthProvider;
import intelink.models.enums.TokenType;
import intelink.models.enums.UserRole;
import intelink.repositories.UserRepository;
import intelink.services.interfaces.IUserServices;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserServices {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;

    @Value("${app.url.verify-email}")
    private String verificationEmailUrlTemplate;

    @Value("${app.url.reset-password}")
    private String resetPasswordEmailUrlTemplate;

    @Transactional
    public User create(String username, String email, String password, UserRole role) throws MessagingException {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .build();

        User savedUser = userRepository.save(user);
        log.info("UserService.create: User created with ID: {}", savedUser.getId());

        VerificationToken verificationToken = verificationTokenService.create(user, TokenType.EMAIL_VERIFICATION, 24);
        String verificationLink = verificationEmailUrlTemplate.replace("{token}", verificationToken.getToken());
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);
        log.info("UserService.create: Verification email sent to {}", user.getEmail());

        return savedUser;
    }

    @Transactional
    public void verifyEmail(String token) {
        Optional<VerificationToken> tokenOpt = verificationTokenService.findValidToken(token, TokenType.EMAIL_VERIFICATION);
        if (tokenOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid or expired verification token");
        }

        VerificationToken verificationToken = tokenOpt.get();

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("UserService.verifyEmail: Email verified for user ID: {}", user.getId());

        verificationTokenService.markTokenAsUsed(verificationToken);
        log.info("UserService.verifyEmail: Verification token marked as used for user ID: {}", user.getId());
    }

    @Transactional
    public void forgotPassword(String email) throws MessagingException {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Do not reveal if the email exists for security reasons
            return;
        }

        User user = userOpt.get();
        VerificationToken verificationToken = verificationTokenService.create(user, TokenType.PASSWORD_RESET, 1);
        String resetLink = resetPasswordEmailUrlTemplate.replace("{token}", verificationToken.getToken());
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);
        log.info("UserService.forgotPassword: Password reset email sent to {}", user.getEmail());
    }

    @Transactional
    public User update(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void incrementTotalClicks(Long userId) {
        userRepository.incrementTotalClicks(userId);
        log.debug("UserService.incrementTotalClicks: Total clicks for user ID {} incremented", userId);
    }

    @Transactional
    public void incrementTotalClicksWithAmount(Long userId, int amount) {
        userRepository.incrementTotalClicksWithAmount(userId, amount);
        log.debug("UserService.incrementTotalClicksWithAmount: Total clicks for user ID {} incremented by {}", userId, amount);
    }

    @Transactional
    public void incrementTotalShortUrls(Long userId) {
        userRepository.incrementTotalShortUrls(userId);
        log.debug("UserService.incrementTotalShortUrls: Total short URLs for user ID {} incremented", userId);
    }

    @Transactional
    public void decrementTotalShortUrls(Long userId) {
        userRepository.decrementTotalShortUrls(userId);
        log.debug("UserService.decrementTotalShortUrls: Total short URLs for user ID {} decremented", userId);
    }


    @Transactional
    public void updateLastLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);
            log.debug("UserService.updateLastLogin: Last login updated for user: {}", username);
        });
    }

    @Transactional(readOnly = true)
    public Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId) {
        return userRepository.findByAuthProviderAndProviderUserId(provider, providerId);
    }

    @Transactional
    public User createOAuthUser(String username, String email, OAuthProvider provider, String providerId) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(null) // OAuth users don't have password
                .role(UserRole.USER)
                .emailVerified(true) // OAuth emails are pre-verified
                .authProvider(provider)
                .providerUserId(providerId)
                .lastLoginAt(Instant.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("UserService.createOAuthUser: OAuth user created with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("UserService.changePassword: Password changed for user: {}", username);
        });
    }
}
