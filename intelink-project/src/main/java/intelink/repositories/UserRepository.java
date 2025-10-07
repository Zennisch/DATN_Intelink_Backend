package intelink.repositories;

import intelink.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.totalClicks = u.totalClicks + 1 WHERE u.id = :userId")
    void increaseTotalClicks(Long userId);

    @Modifying
    @Query("UPDATE User u SET u.totalShortUrls = u.totalShortUrls + 1 WHERE u.id = :userId")
    void increaseTotalShortUrls(Long userId);

    @Modifying
    @Query("UPDATE User u SET u.totalShortUrls = u.totalShortUrls - 1 WHERE u.id = :userId")
    void decreaseTotalShortUrls(Long userId);

}
