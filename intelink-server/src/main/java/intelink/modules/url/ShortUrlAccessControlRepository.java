package intelink.modules.url;

import intelink.models.ShortUrlAccessControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortUrlAccessControlRepository extends JpaRepository<ShortUrlAccessControl, Long> {
}
