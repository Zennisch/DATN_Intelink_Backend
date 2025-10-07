package intelink.repositories;

import intelink.models.ApiKey;
import intelink.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    Optional<ApiKey> findByKeyHash(String keyHash);

    List<ApiKey> findByUser(User user);

    List<ApiKey> findByUserAndActiveTrue(User user);

    List<ApiKey> findByActiveTrue();

    Optional<ApiKey> findByRawKey(String rawKey);
}
