package intelink.services;

import intelink.models.User;
import intelink.models.enums.UserRole;
import intelink.repositories.UserRepository;
import intelink.services.interfaces.IUserServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserServices {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    public User update(User user) {
        return null;
    }

    public Optional<User> findById(Long id) {
        return Optional.empty();
    }

    public Optional<User> findByUsername(String username) {
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        return Optional.empty();
    }

    public boolean existsByUsername(String username) {
        return false;
    }

    public boolean existsByEmail(String email) {
        return false;
    }

    public void incrementTotalClicks(Long userId) {

    }

    public void incrementTotalClicksWithAmount(Long userId, int amount) {

    }

    public void incrementTotalShortUrls(Long userId) {

    }
}
