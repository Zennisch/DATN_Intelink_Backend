package intelink.repositories;

import intelink.models.ClickLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClickLogRepository extends JpaRepository<ClickLog, Long> {
}
