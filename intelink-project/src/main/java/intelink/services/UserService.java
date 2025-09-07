package intelink.services;

import intelink.config.security.JwtTokenProvider;
import intelink.dto.object.AuthObject;
import intelink.dto.object.Token;
import intelink.dto.request.LoginRequest;
import intelink.dto.request.ResetPasswordRequest;
import intelink.models.User;
import intelink.models.VerificationToken;
import intelink.models.enums.UserProvider;
import intelink.models.enums.UserRole;
import intelink.models.enums.VerificationTokenType;
import intelink.repositories.UserRepository;
import intelink.services.interfaces.IEmailService;
import intelink.services.interfaces.IUserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final IEmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.url.verify-email}")
    private String verificationEmailUrlTemplate;

    @Value("${app.url.reset-password}")
    private String resetPasswordEmailUrlTemplate;

    private Token validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Invalid token format");
        }

        String token = authHeader.substring(7);
        String username = jwtTokenProvider.getUsernameFromToken(token);

        if (!jwtTokenProvider.validateToken(token, username)) {
            throw new BadCredentialsException("Invalid or expired token");
        }

        return new Token(token, username);
    }

    @Transactional
    public User register(String username, String email, String password, UserRole role) throws MessagingException {
        // 1. Validate input
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // 2. Create user
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .build();

        User savedUser = userRepository.save(user);
        log.info("UserService.create: User created with ID: {}", savedUser.getId());

        // 3. Generate email verification token and send email
        VerificationToken verificationToken = verificationTokenService.create(user, VerificationTokenType.EMAIL_VERIFICATION, 24);

        String verificationLink = verificationEmailUrlTemplate.replace("{token}", verificationToken.getToken());
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);
        log.info("UserService.create: Verification email sent to {}", user.getEmail());

        return savedUser;
    }

    @Transactional
    public void verifyEmail(String token) {
        Optional<VerificationToken> tokenOpt = verificationTokenService.findValidToken(token, VerificationTokenType.EMAIL_VERIFICATION);
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
        VerificationToken verificationToken = verificationTokenService.create(user, VerificationTokenType.PASSWORD_RESET, 1);
        String resetLink = resetPasswordEmailUrlTemplate.replace("{token}", verificationToken.getToken());
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);
        log.info("UserService.forgotPassword: Password reset email sent to {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(String token, ResetPasswordRequest resetPasswordRequest) {
        if (token == null || token.isEmpty()) {
            throw new BadCredentialsException("Invalid password reset token");
        }

        String password = resetPasswordRequest.getPassword();
        String confirmPassword = resetPasswordRequest.getConfirmPassword();
        if (!password.equals(confirmPassword)) {
            throw new BadCredentialsException("New password and confirmation do not match");
        }

        Optional<VerificationToken> tokenOpt = verificationTokenService.findValidToken(token, VerificationTokenType.PASSWORD_RESET);
        if (tokenOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid or expired password reset token");
        }

        VerificationToken verificationToken = tokenOpt.get();
        User user = verificationToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
        log.info("UserService.resetPassword: Password reset for user ID: {}", user.getId());

        verificationTokenService.markTokenAsUsed(verificationToken);
        log.info("UserService.resetPassword: Password reset token marked as used for user ID: {}", user.getId());
    }

    @Transactional
    public AuthObject login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        Optional<User> userOpt = findByUsername(loginRequest.getUsername());
        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid username or password");
        }

        User user = userOpt.get();

        if (user.getProvider() == UserProvider.LOCAL && !user.getEmailVerified()) {
            throw new BadCredentialsException("Please verify your email before logging in");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(authentication.getName());
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());
        Long expiresAt = jwtTokenProvider.getExpirationTimeFromToken(token);

        return AuthObject.builder()
                .user(user)
                .token(token)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .build();
    }

    @Transactional
    public AuthObject refreshToken(String authHeader) {
        Token tokenObject = validateToken(authHeader);
        String username = tokenObject.getUsername();

        String token = jwtTokenProvider.generateToken(username);
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);
        Long expiresAt = jwtTokenProvider.getExpirationTimeFromToken(token);

        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("User not found");
        }
        User user = userOpt.get();

        return AuthObject.builder()
                .user(user)
                .token(token)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .build();
    }

    @Transactional
    public User profile(String authHeader) {
        Token token = validateToken(authHeader);
        String username = token.getUsername();

        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("User not found");
        }

        User user = userOpt.get();
        log.info("UserService.profile: Retrieved profile for user: {}", username);
        return user;
    }

    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Invalid token format");
        }

        String token = authHeader.substring(7);
        String username = jwtTokenProvider.getUsernameFromToken(token);

        if (jwtTokenProvider.validateToken(token, username)) {
            SecurityContextHolder.clearContext();
        }
    }

    @Transactional
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void incrementTotalClicks(Long userId) {
        userRepository.incrementTotalClicks(userId);
        log.debug("UserService.incrementTotalClicks: Total clicks for user ID {} incremented", userId);
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
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
