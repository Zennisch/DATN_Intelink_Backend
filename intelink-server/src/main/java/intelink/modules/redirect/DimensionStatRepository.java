package intelink.modules.redirect;

import intelink.models.DimensionStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DimensionStatRepository extends JpaRepository<DimensionStat, UUID> {
}
