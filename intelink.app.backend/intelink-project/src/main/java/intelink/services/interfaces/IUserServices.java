package intelink.services.interfaces;

import intelink.models.User;
import intelink.models.enums.UserRole;

import java.util.Optional;

public interface IUserServices {

    User create(String username, String email, String password, UserRole role);

    User update(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void incrementTotalClicks(Long userId);

    void incrementTotalClicksWithAmount(Long userId, int amount);

    void incrementTotalShortUrls(Long userId);

}
