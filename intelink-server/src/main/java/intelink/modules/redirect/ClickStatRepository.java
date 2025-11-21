package intelink.modules.redirect;

import intelink.models.ClickStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClickStatRepository extends JpaRepository<ClickStat, UUID> {
}
