package com.xypay.xypay.repository;

import com.xypay.xypay.domain.LargeTransactionShieldSettings;
import com.xypay.xypay.domain.User;
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
public interface LargeTransactionShieldSettingsRepository extends JpaRepository<LargeTransactionShieldSettings, UUID> {
    
    /**
     * Find shield settings by user
     */
    Optional<LargeTransactionShieldSettings> findByUser(User user);
    
    /**
     * Find shield settings by user ID
     */
    Optional<LargeTransactionShieldSettings> findByUserId(UUID userId);
    
    /**
     * Find all enabled shield settings
     */
    List<LargeTransactionShieldSettings> findByEnabledTrue();
    
    /**
     * Count active shields
     */
    long countByEnabledTrue();
    
    /**
     * Find shields that require verification for given amount
     */
    @Query("SELECT s FROM LargeTransactionShieldSettings s WHERE s.enabled = true AND s.perTransactionLimit IS NOT NULL AND s.perTransactionLimit < :amount")
    List<LargeTransactionShieldSettings> findShieldsRequiringVerification(@Param("amount") BigDecimal amount);
    
    /**
     * Find shields with face recognition enabled
     */
    @Query("SELECT s FROM LargeTransactionShieldSettings s WHERE s.enabled = true AND s.faceTemplateHash IS NOT NULL")
    List<LargeTransactionShieldSettings> findShieldsWithFaceRecognition();
    
    /**
     * Count shields registered in date range
     */
    @Query("SELECT COUNT(s) FROM LargeTransactionShieldSettings s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find shields by transaction limit range
     */
    @Query("SELECT s FROM LargeTransactionShieldSettings s WHERE s.enabled = true AND s.perTransactionLimit BETWEEN :minAmount AND :maxAmount")
    List<LargeTransactionShieldSettings> findByTransactionLimitRange(@Param("minAmount") BigDecimal minAmount, @Param("maxAmount") BigDecimal maxAmount);
}
