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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public User register(RegisterRequest registerRequest, UserRole role) throws MessagingException {
        // 0. Extract fields
        String username = registerRequest.username();
        String email = registerRequest.email();
        String password = registerRequest.password();

        // 1. Validate input
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // 2. Create user
        User user = User.builder().username(username).email(email).password(passwordEncoder.encode(password)).role(role).build();

        User savedUser = userRepository.save(user);
        log.info("UserService.create: User created with ID: {}", savedUser.getId());

        // 3. Create FREE subscription for new user

        // 4. Generate email verification token and send email
        VerificationToken verificationToken = verificationTokenService.createToken(user, VerificationTokenType.EMAIL_VERIFICATION, 24);

        String verificationLink = verificationEmailUrlTemplate.replace("{token}", verificationToken.getToken());
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);
        log.info("UserService.create: Verification email sent to {}", user.getEmail());

        return savedUser;
    }

}
