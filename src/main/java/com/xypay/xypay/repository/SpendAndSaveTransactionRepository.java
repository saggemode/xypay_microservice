package com.xypay.xypay.repository;

import com.xypay.xypay.domain.SpendAndSaveTransaction;
import com.xypay.xypay.domain.SpendAndSaveAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SpendAndSaveTransactionRepository extends JpaRepository<SpendAndSaveTransaction, Long> {
    
    @Query("SELECT t FROM SpendAndSaveTransaction t WHERE t.spendAndSaveAccount = :account ORDER BY t.createdAt DESC")
    List<SpendAndSaveTransaction> findBySpendAndSaveAccountOrderByCreatedAtDesc(@Param("account") SpendAndSaveAccount account);
    
    @Query("SELECT t FROM SpendAndSaveTransaction t WHERE t.spendAndSaveAccount.id = :accountId")
    List<SpendAndSaveTransaction> findBySpendAndSaveAccountId(@Param("accountId") Long accountId);
    
    List<SpendAndSaveTransaction> findByTransactionType(String transactionType);
    
    List<SpendAndSaveTransaction> findByStatus(String status);
    
    List<SpendAndSaveTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM SpendAndSaveTransaction t WHERE t.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT SUM(t.amount) FROM SpendAndSaveTransaction t WHERE t.status = 'COMPLETED'")
    BigDecimal getTotalCompletedAmount();
    
    @Query("SELECT t FROM SpendAndSaveTransaction t WHERE t.spendAndSaveAccount.user.id = :userId ORDER BY t.createdAt DESC")
    List<SpendAndSaveTransaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
