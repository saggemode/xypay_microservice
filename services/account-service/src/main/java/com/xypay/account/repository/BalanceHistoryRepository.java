package com.xypay.account.repository;

import com.xypay.account.domain.BalanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BalanceHistoryRepository extends JpaRepository<BalanceHistory, UUID> {
    
    List<BalanceHistory> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
    
    @Query("SELECT bh FROM BalanceHistory bh WHERE bh.accountId = :accountId AND bh.createdAt BETWEEN :startDate AND :endDate ORDER BY bh.createdAt DESC")
    List<BalanceHistory> findByAccountIdAndDateRange(@Param("accountId") UUID accountId, 
                                                    @Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT bh FROM BalanceHistory bh WHERE bh.accountId = :accountId AND bh.reason = :reason ORDER BY bh.createdAt DESC")
    List<BalanceHistory> findByAccountIdAndReason(@Param("accountId") UUID accountId, @Param("reason") String reason);
}
