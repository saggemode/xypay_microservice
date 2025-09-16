package com.xypay.xypay.repository;

import com.xypay.xypay.domain.CBNLevy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CBNLevyRepository extends JpaRepository<CBNLevy, UUID> {
    
    /**
     * Find all active levies
     */
    List<CBNLevy> findByIsActiveTrue();
    
    /**
     * Find all active levies with pagination
     */
    Page<CBNLevy> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Find levies by transaction type
     */
    List<CBNLevy> findByTransactionTypeAndIsActiveTrue(String transactionType);
    
    /**
     * Find levies effective for a specific date
     */
    @Query("SELECT l FROM CBNLevy l WHERE l.isActive = true AND " +
           "(l.effectiveFrom IS NULL OR l.effectiveFrom <= :date) AND " +
           "(l.effectiveTo IS NULL OR l.effectiveTo >= :date)")
    List<CBNLevy> findEffectiveLevies(@Param("date") LocalDateTime date);
    
    /**
     * Find levy by name
     */
    Optional<CBNLevy> findByName(String name);
    
    /**
     * Find levies by regulation reference
     */
    List<CBNLevy> findByRegulationReference(String regulationReference);
    
    /**
     * Find levies with amount range
     */
    @Query("SELECT l FROM CBNLevy l WHERE l.isActive = true AND " +
           "(:minAmount IS NULL OR l.minAmount <= :minAmount) AND " +
           "(:maxAmount IS NULL OR l.maxAmount IS NULL OR l.maxAmount >= :maxAmount)")
    List<CBNLevy> findApplicableLevies(@Param("minAmount") BigDecimal minAmount, 
                                      @Param("maxAmount") BigDecimal maxAmount);
    
    /**
     * Count active levies
     */
    long countByIsActiveTrue();
    
    /**
     * Find levies expiring soon
     */
    @Query("SELECT l FROM CBNLevy l WHERE l.isActive = true AND " +
           "l.effectiveTo IS NOT NULL AND l.effectiveTo <= :expiryDate")
    List<CBNLevy> findExpiringSoon(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Find levies by name containing (for search)
     */
    @Query("SELECT l FROM CBNLevy l WHERE l.name LIKE CONCAT('%', :name, '%') OR l.description LIKE CONCAT('%', :name, '%')")
    Page<CBNLevy> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
}
