package intelink.services;

import intelink.config.security.JwtTokenProvider;
import intelink.dto.object.Auth;
import intelink.dto.object.Token;
import intelink.dto.request.auth.LoginRequest;
import intelink.dto.request.auth.RegisterRequest;
import intelink.dto.request.auth.ResetPasswordRequest;
import intelink.models.User;
import intelink.models.VerificationToken;
import intelink.models.enums.UserProvider;
import intelink.models.enums.UserRole;
import intelink.models.enums.VerificationTokenType;
import intelink.repositories.UserRepository;
import intelink.services.interfaces.IEmailService;
import intelink.services.interfaces.IUserService;
import intelink.services.interfaces.IVerificationTokenService;
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
    private final IVerificationTokenService verificationTokenService;
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
    public User register(RegisterRequest registerRequest, UserRole role) throws MessagingException {
        // 0. Extract fields
        String username = registerRequest.getUsername();
        String email = registerRequest.getEmail();
        String password = registerRequest.getPassword();

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
        VerificationToken verificationToken = verificationTokenService.createToken(user, VerificationTokenType.EMAIL_VERIFICATION, 24);

        String verificationLink = verificationEmailUrlTemplate.replace("{token}", verificationToken.getToken());
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);
        log.info("UserService.create: Verification email sent to {}", user.getEmail());

        return savedUser;
    }

    @Transactional
    public void verifyEmail(String token) {
        // 1. Validate token
        Optional<VerificationToken> tokenOpt = verificationTokenService.findValidToken(token, VerificationTokenType.EMAIL_VERIFICATION);
        if (tokenOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid or expired verification token");
        }

        // 2. Mark token as used
        VerificationToken verificationToken = tokenOpt.get();
        User user = verificationToken.getUser();
        verificationTokenService.markAsUsed(verificationToken);
        log.info("UserService.verifyEmail: Verification token marked as used for user ID: {}", user.getId());

        // 3. Mark user's email as verified
        user.setEmailVerified(true);
        userRepository.save(user);
        log.info("UserService.verifyEmail: Email verified for user ID: {}", user.getId());
    }

    @Transactional
    public void forgotPassword(String email) throws MessagingException {
        // 1. Check if user with the email exists
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Do not reveal if the email exists for security reasons
            return;
        }

        // 2. If exists, generate password reset token
        User user = userOpt.get();
        VerificationToken verificationToken = verificationTokenService.createToken(user, VerificationTokenType.PASSWORD_RESET, 1);
        String resetLink = resetPasswordEmailUrlTemplate.replace("{token}", verificationToken.getToken());

        // 3. Send password reset email
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);
        log.info("UserService.forgotPassword: Password reset email sent to {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(String token, ResetPasswordRequest resetPasswordRequest) {
        // 1. Validate token and passwords
        if (token == null || token.isEmpty()) {
            throw new BadCredentialsException("Invalid password reset token");
        }

        Optional<VerificationToken> tokenOpt = verificationTokenService.findValidToken(token, VerificationTokenType.PASSWORD_RESET);
        if (tokenOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid or expired password reset token");
        }

        String password = resetPasswordRequest.getPassword();
        String confirmPassword = resetPasswordRequest.getConfirmPassword();
        if (!password.equals(confirmPassword)) {
            throw new BadCredentialsException("New password and confirmation do not match");
        }

        // 2. Mark token as used
        VerificationToken verificationToken = tokenOpt.get();
        User user = verificationToken.getUser();
        verificationTokenService.markAsUsed(verificationToken);
        log.info("UserService.resetPassword: Password reset token marked as used for user ID: {}", user.getId());

        // 4. Update user's password
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
        log.info("UserService.resetPassword: Password reset for user ID: {}", user.getId());
    }

    @Transactional
    public Auth login(LoginRequest loginRequest) {
        // 0. Extract fields
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 1. Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        // 2. Check if user exists for this username
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // 3. If exists, check if email is verified for LOCAL users
        User user = userOpt.get();
        if (user.getProvider() == UserProvider.LOCAL && !user.getEmailVerified()) {
            throw new BadCredentialsException("Please verify your email before logging in");
        }

        // 4. Update last login time
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // 5. Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication.getName());
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());
        Long expiresAt = jwtTokenProvider.getExpirationTimeFromToken(token);

        return new Auth(user, token, refreshToken, expiresAt);
    }

    @Transactional
    public Auth refreshToken(String authHeader) {
        // 1. Validate token
        Token tokenObject = validateToken(authHeader);
        String username = tokenObject.getUsername();

        // 2. If valid, check if user exists
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("User not found");
        }

        // 3. If exists, generate new JWT token
        User user = userOpt.get();
        String token = jwtTokenProvider.generateToken(username);
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);
        Long expiresAt = jwtTokenProvider.getExpirationTimeFromToken(token);

        return new Auth(user, token, refreshToken, expiresAt);
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
