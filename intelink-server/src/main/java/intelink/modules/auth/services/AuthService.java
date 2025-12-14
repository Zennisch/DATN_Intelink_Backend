package intelink.modules.auth.services;

import intelink.configs.securities.JwtTokenProvider;
import intelink.dto.auth.LoginRequest;
import intelink.dto.auth.RegisterRequest;
import intelink.dto.auth.ResetPasswordRequest;
import intelink.dto.auth.UpdatePasswordRequest;
import intelink.dto.auth.UpdateProfileRequest;
import intelink.dto.auth.UserProfileResponse;
import intelink.models.Subscription;
import intelink.models.SubscriptionPlan;
import intelink.models.User;
import intelink.models.VerificationToken;
import intelink.models.enums.SubscriptionPlanBillingInterval;
import intelink.models.enums.SubscriptionPlanType;
import intelink.models.enums.SubscriptionStatus;
import intelink.models.enums.UserRole;
import intelink.models.enums.VerificationTokenType;
import intelink.modules.auth.repositories.UserRepository;
import intelink.modules.subscription.repositories.SubscriptionPlanRepository;
import intelink.modules.subscription.repositories.SubscriptionRepository;
import intelink.modules.utils.services.EmailService;
import intelink.utils.helper.AuthToken;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.url.template.verify-email}")
    private String verifyEmailUrlTemplate;

    @Value("${app.url.template.reset-password}")
    private String resetPasswordUrlTemplate;

    private void sendVerificationEmail(User user) throws MessagingException {
        VerificationToken verificationToken = verificationTokenService.createToken(user, VerificationTokenType.EMAIL_VERIFICATION, 24);
        String verificationLink = verifyEmailUrlTemplate.replace("{token}", verificationToken.getToken());
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
        if (existingUserByEmail.isPresent() && existingUserByUsername.isPresent() && existingUserByEmail.get().getId().equals(existingUserByUsername.get().getId())) {
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
        try {
            Optional<SubscriptionPlan> freePlanOpt = subscriptionPlanRepository.findByTypeAndBillingInterval(
                SubscriptionPlanType.FREE, 
                SubscriptionPlanBillingInterval.NONE
            );
            if (freePlanOpt.isPresent()) {
                SubscriptionPlan freePlan = freePlanOpt.get();
                Subscription freeSubscription = Subscription.builder().user(savedUser).subscriptionPlan(freePlan).status(SubscriptionStatus.ACTIVE).active(true).activatedAt(Instant.now()).expiresAt(null) // Lifetime for FREE plan
                        .creditUsed(0.0).build();
                subscriptionRepository.save(freeSubscription);
                log.info("[UserService] FREE subscription created for user ID: {}", savedUser.getId());
            } else {
                log.warn("[UserService] FREE plan not found. Skipping subscription creation for user ID: {}", savedUser.getId());
            }
        } catch (Exception e) {
            log.error("[UserService] Failed to create FREE subscription for user ID: {}. Error: {}", savedUser.getId(), e.getMessage());
            // Don't fail registration if subscription creation fails
        }

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
        String resetLink = resetPasswordUrlTemplate.replace("{token}", verificationToken.getToken());

        // 3. Send password reset email
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);
        log.info("[UserService] Password reset email sent to {}", user.getEmail());
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

        // 2. Check if token already used
        if (tokenOpt.get().getUsed()) {
            throw new BadCredentialsException("Password reset token has already been used");
        }

        String password = resetPasswordRequest.password();
        String confirmPassword = resetPasswordRequest.confirmPassword();
        if (!password.equals(confirmPassword)) {
            throw new BadCredentialsException("New password and confirmation do not match");
        }

        // 3. Set token as used
        VerificationToken verificationToken = tokenOpt.get();
        User user = verificationToken.getUser();
        verificationTokenService.setTokenAsUsed(verificationToken);
        log.info("UserService.resetPassword: Password reset token marked as used for user ID: {}", user.getId());

        // 4. Update user's password
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        log.info("UserService.resetPassword: Password reset for user ID: {}", user.getId());
    }

    @Transactional
    public AuthToken login(LoginRequest loginRequest) {
        // 0. Extract fields
        String username = loginRequest.username();
        String password = loginRequest.password();

        // 1. Authenticate user
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        // 2. Check if user exists for this username
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(username, username);
        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // 3. If exists, check if email is verified for local users
        User user = userOpt.get();
        if (!user.getVerified()) {
            throw new BadCredentialsException("Please verify your email before logging in");
        }

        // 4. Update last login time
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // 5. Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication.getName());
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());
        Long expiresAt = jwtTokenProvider.getExpirationTimeFromToken(token);

        return new AuthToken(user, token, refreshToken, expiresAt);
    }

    @Transactional(readOnly = true)
    public AuthToken refreshToken(User user) {
        String username = user.getUsername();
        String token = jwtTokenProvider.generateToken(username);
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);
        Long expiresAt = jwtTokenProvider.getExpirationTimeFromToken(token);

        return new AuthToken(user, token, refreshToken, expiresAt);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(User user) {
        Optional<Subscription> activeSubscription = subscriptionRepository.findActiveSubscriptionByUser(user);
        return UserProfileResponse.fromEntity(user, activeSubscription.orElse(null));
    }

    @Transactional
    public UserProfileResponse updateProfile(User user, UpdateProfileRequest request) {
        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsernameAndVerifiedTrue(request.username())) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(request.username());
        }

        if (request.profileName() != null) {
            user.setProfileName(request.profileName());
        }

        if (request.profilePictureUrl() != null) {
            user.setProfilePictureUrl(request.profilePictureUrl());
        }

        User updatedUser = userRepository.save(user);
        return getProfile(updatedUser);
    }

    @Transactional
    public void updatePassword(User user, UpdatePasswordRequest request) {
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public void logout(User user) {
        // For JWT, logout is typically handled on the client side by deleting the token.
        // Optionally, you can implement token blacklisting here if needed.
        SecurityContextHolder.clearContext();
        log.info("UserService.logout: User ID {} logged out", user.getId());
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("UserService.getCurrentUser: Username from context: {}", username);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        return userOpt.get();
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(UserDetails userDetails) {
        String username = userDetails.getUsername();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        return userOpt.get();
    }

    @Transactional
    public void increaseTotalShortUrls(Long userId) {
        userRepository.incrementTotalShortUrls(userId);
    }

    @Transactional
    public void decreaseTotalShortUrls(Long userId) {
        userRepository.decrementTotalShortUrls(userId);
    }

    @Transactional
    public void increaseTotalClicks(Long userId) {
        userRepository.incrementTotalClicks(userId);
    }
}
