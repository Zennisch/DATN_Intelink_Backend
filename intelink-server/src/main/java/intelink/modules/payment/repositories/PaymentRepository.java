package intelink.modules.payment.repositories;

import intelink.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.subscription WHERE p.id = :id")
    Optional<Payment> findByIdWithSubscription(@Param("id") UUID id);
    
    @Query("SELECT p FROM Payment p LEFT JOIN FETCH p.subscription WHERE p.transactionId = :transactionId")
    Optional<Payment> findByTransactionIdWithSubscription(@Param("transactionId") String transactionId);
}
