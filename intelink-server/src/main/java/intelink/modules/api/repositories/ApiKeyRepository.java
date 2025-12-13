package intelink.modules.api.repositories;

import intelink.models.ApiKey;
import intelink.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    List<ApiKey> findByUser(User user);
    Optional<ApiKey> findByIdAndUser(UUID id, User user);
    boolean existsByRawKey(String rawKey);
}
