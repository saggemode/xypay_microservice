package com.xypay.xypay.repository;

import com.xypay.xypay.domain.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, UUID> {
    
    List<Charge> findByTransactionOrderByCreatedAtDesc(UUID transactionId);
    
    List<Charge> findByStatusOrderByCreatedAtDesc(String status);
    
    List<Charge> findByWaivedByOrderByCreatedAtDesc(UUID waivedById);
    
    @Query("SELECT c FROM Charge c WHERE c.transaction.id = :transactionId")
    Optional<Charge> findByTransactionId(@Param("transactionId") UUID transactionId);
    
    @Query("SELECT c FROM Charge c WHERE c.status = 'waived' AND c.waivedBy.id = :userId ORDER BY c.createdAt DESC")
    List<Charge> findWaivedChargesByUser(@Param("userId") UUID userId);
}
