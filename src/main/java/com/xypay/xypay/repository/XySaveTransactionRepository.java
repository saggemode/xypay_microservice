package com.xypay.xypay.repository;

import com.xypay.xypay.domain.XySaveTransaction;
import com.xypay.xypay.domain.XySaveAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface XySaveTransactionRepository extends JpaRepository<XySaveTransaction, UUID> {
    
    List<XySaveTransaction> findByXysaveAccountOrderByCreatedAtDesc(XySaveAccount account);
    
    List<XySaveTransaction> findByXysaveAccountId(UUID accountId);
    
    List<XySaveTransaction> findByTransactionType(String transactionType);
    
    // List<XySaveTransaction> findByStatus(String status);
    
    List<XySaveTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // @Query("SELECT COUNT(t) FROM XySaveTransaction t WHERE t.status = :status")
    // Long countByStatus(@Param("status") String status);
    
    Page<XySaveTransaction> findByXysaveAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);
    
    List<XySaveTransaction> findByXysaveAccountIdOrderByCreatedAtDesc(UUID accountId);
    
    // Custom queries for user-based lookups
    @Query("SELECT t FROM XySaveTransaction t WHERE t.xysaveAccount.user.id = :userId ORDER BY t.createdAt DESC")
    Page<XySaveTransaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT t FROM XySaveTransaction t WHERE t.xysaveAccount.user.id = :userId AND t.transactionType = :transactionType ORDER BY t.createdAt DESC")
    Page<XySaveTransaction> findByUserIdAndTransactionTypeOrderByCreatedAtDesc(@Param("userId") UUID userId, @Param("transactionType") String transactionType, Pageable pageable);
    
    @Query("SELECT t FROM XySaveTransaction t WHERE t.xysaveAccount.user.id = :userId ORDER BY t.createdAt DESC")
    List<XySaveTransaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);
    
    // Find by reference
    XySaveTransaction findByReference(String reference);
    
    // Find by amount range
    List<XySaveTransaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    // Find by date range and user
    @Query("SELECT t FROM XySaveTransaction t WHERE t.xysaveAccount.user.id = :userId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<XySaveTransaction> findByUserIdAndCreatedAtBetween(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}