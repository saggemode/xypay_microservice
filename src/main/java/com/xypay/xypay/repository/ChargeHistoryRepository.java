package com.xypay.xypay.repository;

import com.xypay.xypay.domain.ChargeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChargeHistoryRepository extends JpaRepository<ChargeHistory, UUID> {
    
    List<ChargeHistory> findByChargeTypeOrderByCreatedAtDesc(ChargeHistory.ChargeType chargeType);
    
    List<ChargeHistory> findByChangedBy_IdOrderByCreatedAtDesc(UUID changedById);
    
    List<ChargeHistory> findByEffectiveFromBetweenOrderByEffectiveFromDesc(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT ch FROM ChargeHistory ch WHERE ch.chargeType = :chargeType AND ch.effectiveFrom >= :startDate ORDER BY ch.effectiveFrom DESC")
    List<ChargeHistory> findByChargeTypeAndEffectiveFromAfter(@Param("chargeType") ChargeHistory.ChargeType chargeType, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT ch FROM ChargeHistory ch WHERE ch.changedBy.id = :userId AND ch.effectiveFrom >= :startDate ORDER BY ch.effectiveFrom DESC")
    List<ChargeHistory> findByChangedByAndEffectiveFromAfter(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate);
}
