package intelink.repositories;

import intelink.models.CustomDomain;
import intelink.models.User;
import intelink.models.enums.CustomDomainStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomDomainRepository extends JpaRepository<CustomDomain, UUID> {

    Optional<CustomDomain> findByDomain(String domain);

    List<CustomDomain> findByUser(User user);

    List<CustomDomain> findByStatus(CustomDomainStatus status);

    List<CustomDomain> findByUserAndActiveTrue(User user);

    List<CustomDomain> findByVerifiedTrue();

}
