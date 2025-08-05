package intelink.services;

import intelink.models.User;
import intelink.models.enums.UserRole;
import intelink.repositories.UserRepository;
import intelink.services.interfaces.IUserServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserServices {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User create(String username, String email, String password, UserRole role) {
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
        return savedUser;
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
}
