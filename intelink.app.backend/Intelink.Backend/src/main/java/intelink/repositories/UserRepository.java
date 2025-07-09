package intelink.repositories;

import intelink.models.User;
import intelink.models.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);

    @Modifying
    @Query("UPDATE User u SET u.totalClicks = u.totalClicks + :increment WHERE u.id = :userId")
    void incrementTotalClicks(@Param("userId") Long userId, @Param("increment") Long increment);

    @Modifying
    @Query("UPDATE User u SET u.totalShortUrls = u.totalShortUrls + 1 WHERE u.id = :userId")
    void incrementTotalShortUrls(@Param("userId") Long userId);
}