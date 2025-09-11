package com.xypay.transaction.repository;

import com.xypay.transaction.domain.Transaction;
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

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Find transactions by account number
    List<Transaction> findByAccountNumberOrderByTimestampDesc(String accountNumber);
    
    // Find transactions by account number with pagination
    Page<Transaction> findByAccountNumberOrderByTimestampDesc(String accountNumber, Pageable pageable);
    
    // Find transactions by reference
    Optional<Transaction> findByReference(String reference);
    
    // Find transactions by idempotency key
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    
    // Find transactions by status
    List<Transaction> findByStatusOrderByTimestampDesc(com.xypay.transaction.enums.TransactionStatus status);
    
    // Find transactions by type
    List<Transaction> findByTypeOrderByTimestampDesc(com.xypay.transaction.enums.TransactionType type);
    
    // Find transactions by channel
    List<Transaction> findByChannelOrderByTimestampDesc(com.xypay.transaction.enums.TransactionChannel channel);
    
    // Find transactions by date range
    List<Transaction> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find transactions by wallet and date range
    List<Transaction> findByWalletIdAndTimestampBetweenOrderByTimestampDesc(
        Long walletId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find transactions by amount range
    List<Transaction> findByAmountBetweenOrderByTimestampDesc(BigDecimal minAmount, BigDecimal maxAmount);
    
    // Find transactions by currency
    List<Transaction> findByCurrencyOrderByTimestampDesc(String currency);
    
    // Find transactions by parent ID (for grouped transactions)
    List<Transaction> findByParentIdOrderByTimestampDesc(Long parentId);
    
    // Find transactions by receiver ID
    List<Transaction> findByReceiverIdOrderByTimestampDesc(Long receiverId);
    
    // Find successful transactions by wallet
    List<Transaction> findByWalletIdAndStatusOrderByTimestampDesc(Long walletId, String status);
    
    // Find pending transactions
    List<Transaction> findByStatusOrderByTimestampAsc(String status);
    
    // Count transactions by wallet and status
    long countByWalletIdAndStatus(Long walletId, String status);
    
    // Sum amount by wallet and type
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.walletId = :walletId AND t.type = :type AND t.status = 'SUCCESS'")
    BigDecimal sumAmountByWalletIdAndType(@Param("walletId") Long walletId, @Param("type") String type);
    
    // Sum amount by wallet and channel
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.walletId = :walletId AND t.channel = :channel AND t.status = 'SUCCESS'")
    BigDecimal sumAmountByWalletIdAndChannel(@Param("walletId") Long walletId, @Param("channel") String channel);
    
    // Find transactions by wallet with filters
    @Query("SELECT t FROM Transaction t WHERE t.walletId = :walletId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:channel IS NULL OR t.channel = :channel) " +
           "AND (:startDate IS NULL OR t.timestamp >= :startDate) " +
           "AND (:endDate IS NULL OR t.timestamp <= :endDate) " +
           "ORDER BY t.timestamp DESC")
    Page<Transaction> findTransactionsWithFilters(
        @Param("walletId") Long walletId,
        @Param("status") String status,
        @Param("type") String type,
        @Param("channel") String channel,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
    
    // Find recent transactions by wallet (last N transactions)
    @Query("SELECT t FROM Transaction t WHERE t.walletId = :walletId ORDER BY t.timestamp DESC")
    List<Transaction> findRecentTransactionsByWalletId(@Param("walletId") Long walletId, Pageable pageable);
    
    // Find transactions by multiple wallet IDs
    List<Transaction> findByWalletIdInOrderByTimestampDesc(List<Long> walletIds);
    
    // Find transactions by reference pattern
    List<Transaction> findByReferenceContainingIgnoreCaseOrderByTimestampDesc(String referencePattern);
    
    // Find transactions by description pattern
    List<Transaction> findByDescriptionContainingIgnoreCaseOrderByTimestampDesc(String descriptionPattern);
    
    // Find retryable transactions
    @Query("SELECT t FROM Transaction t WHERE t.status = 'FAILED' AND t.processedAt < :retryThreshold AND " +
           "(t.metadata IS NULL OR t.metadata NOT LIKE '%maxRetriesReached%')")
    List<Transaction> findRetryableTransactions(@Param("retryThreshold") LocalDateTime retryThreshold);
    
    // Find transactions by timestamp range and status
    List<Transaction> findByTimestampBetweenAndStatusOrderByTimestampDesc(
        LocalDateTime startDate, LocalDateTime endDate, TransactionStatus status);
}
