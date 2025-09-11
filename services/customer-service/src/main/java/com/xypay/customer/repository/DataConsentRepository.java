package com.xypay.customer.repository;

import com.xypay.customer.domain.DataConsent;
import com.xypay.customer.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DataConsentRepository extends JpaRepository<DataConsent, Long> {
    
    List<DataConsent> findByUser(User user);
    
    Optional<DataConsent> findByUserAndConsentType(User user, DataConsent.ConsentType consentType);
    
    List<DataConsent> findByConsentType(DataConsent.ConsentType consentType);
    
    List<DataConsent> findByStatus(DataConsent.ConsentStatus status);
    
    @Query("SELECT c FROM DataConsent c WHERE c.user.id = :userId AND c.consentType = :consentType")
    Optional<DataConsent> findByUserIdAndConsentType(@Param("userId") Long userId, @Param("consentType") DataConsent.ConsentType consentType);
    
    @Query("SELECT c FROM DataConsent c WHERE c.user.id = :userId AND c.status = 'GRANTED' AND (c.expiresAt IS NULL OR c.expiresAt > :now)")
    List<DataConsent> findActiveConsentsByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM DataConsent c WHERE c.expiresAt IS NOT NULL AND c.expiresAt < :now AND c.status = 'GRANTED'")
    List<DataConsent> findExpiredConsents(@Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM DataConsent c WHERE c.createdAt >= :startDate AND c.createdAt <= :endDate")
    List<DataConsent> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(c) FROM DataConsent c WHERE c.consentType = :consentType AND c.status = 'GRANTED'")
    Long countActiveConsentsByType(@Param("consentType") DataConsent.ConsentType consentType);
    
    @Query("SELECT COUNT(c) FROM DataConsent c WHERE c.user.id = :userId AND c.status = 'GRANTED'")
    Long countActiveConsentsByUserId(@Param("userId") Long userId);
}
