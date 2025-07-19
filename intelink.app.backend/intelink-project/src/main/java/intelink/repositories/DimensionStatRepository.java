package intelink.repositories;

import intelink.models.DimensionStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DimensionStatRepository extends JpaRepository<DimensionStat, Long> {
}
