package com.xypay.xypay.repository;

import com.xypay.xypay.domain.XySaveTransaction;
import com.xypay.xypay.domain.XySaveAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface XySaveTransactionRepository extends JpaRepository<XySaveTransaction, Long> {
    
    List<XySaveTransaction> findByXySaveAccountOrderByCreatedAtDesc(XySaveAccount account);
    
    List<XySaveTransaction> findByXySaveAccountId(Long accountId);
    
    List<XySaveTransaction> findByTransactionType(String transactionType);
    
    // List<XySaveTransaction> findByStatus(String status);
    
    List<XySaveTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // @Query("SELECT COUNT(t) FROM XySaveTransaction t WHERE t.status = :status")
    // Long countByStatus(@Param("status") String status);
    
    @Query("SELECT SUM(t.amount) FROM XySaveTransaction t WHERE t.transactionType = 'DEPOSIT' OR t.transactionType = 'INTEREST_CREDIT'")
    BigDecimal getTotalCompletedAmount();
    
    @Query("SELECT t FROM XySaveTransaction t WHERE t.xySaveAccount.user.id = :userId ORDER BY t.createdAt DESC")
    List<XySaveTransaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}