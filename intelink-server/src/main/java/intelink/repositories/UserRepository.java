package intelink.repositories;

import intelink.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameAndVerifiedFalse(String username);

    Optional<User> findByEmailAndVerifiedFalse(String email);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndVerifiedTrue(String username);

    boolean existsByEmailAndVerifiedTrue(String email);

    @Modifying
    @Query("DELETE FROM User u WHERE u.verified = false AND u.createdAt < :threshold")
    int deleteUnverifiedUsersBefore(@Param("threshold") LocalDateTime threshold);
}
