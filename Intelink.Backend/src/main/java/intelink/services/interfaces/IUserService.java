package intelink.services.interfaces;

import intelink.models.User;
import intelink.models.enums.UserRole;

import java.util.Optional;

public interface IUserService {

    User createUser(String username, String email, String password, UserRole role);

    User updateUser(User user);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countUsersByRole(UserRole role);

    void incrementClickCount(Long userId, Long clickIncrement);

    void incrementUrlCount(Long userId);

}
