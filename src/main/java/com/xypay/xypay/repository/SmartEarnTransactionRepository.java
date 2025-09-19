package com.xypay.xypay.repository;

import com.xypay.xypay.domain.SmartEarnAccount;
import com.xypay.xypay.domain.SmartEarnTransaction;
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
public interface SmartEarnTransactionRepository extends JpaRepository<SmartEarnTransaction, UUID> {
    
    /**
     * Find transactions by SmartEarn account
     */
    List<SmartEarnTransaction> findBySmartEarnAccountOrderByTransactionTimeDesc(SmartEarnAccount smartEarnAccount);
    
    /**
     * Find transactions by SmartEarn account with pagination
     */
    Page<SmartEarnTransaction> findBySmartEarnAccountOrderByTransactionTimeDesc(SmartEarnAccount smartEarnAccount, Pageable pageable);
    
    /**
     * Find transactions by reference
     */
    Optional<SmartEarnTransaction> findByReference(String reference);
    
    /**
     * Find transactions by type
     */
    List<SmartEarnTransaction> findByTransactionTypeOrderByTransactionTimeDesc(SmartEarnTransaction.TransactionType transactionType);
    
    /**
     * Find transactions by type with pagination
     */
    Page<SmartEarnTransaction> findByTransactionType(SmartEarnTransaction.TransactionType transactionType, Pageable pageable);
    
    /**
     * Find transactions by status
     */
    List<SmartEarnTransaction> findByStatusOrderByTransactionTimeDesc(SmartEarnTransaction.TransactionStatus status);
    
    /**
     * Find transactions by status with pagination
     */
    Page<SmartEarnTransaction> findByStatus(SmartEarnTransaction.TransactionStatus status, Pageable pageable);
    
    /**
     * Find transactions by status and type with pagination
     */
    Page<SmartEarnTransaction> findByStatusAndTransactionType(SmartEarnTransaction.TransactionStatus status, 
                                                             SmartEarnTransaction.TransactionType transactionType, 
                                                             Pageable pageable);
    
    /**
     * Find transactions by SmartEarn account and type
     */
    List<SmartEarnTransaction> findBySmartEarnAccountAndTransactionTypeOrderByTransactionTimeDesc(
            SmartEarnAccount smartEarnAccount, 
            SmartEarnTransaction.TransactionType transactionType);
    
    /**
     * Find transactions by SmartEarn account and status
     */
    List<SmartEarnTransaction> findBySmartEarnAccountAndStatusOrderByTransactionTimeDesc(
            SmartEarnAccount smartEarnAccount, 
            SmartEarnTransaction.TransactionStatus status);
    
    /**
     * Find transactions between dates
     */
    @Query("SELECT st FROM SmartEarnTransaction st WHERE st.smartEarnAccount = :account AND st.transactionTime BETWEEN :startDate AND :endDate ORDER BY st.transactionTime DESC")
    List<SmartEarnTransaction> findTransactionsBetweenDates(@Param("account") SmartEarnAccount account,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find pending transactions
     */
    @Query("SELECT st FROM SmartEarnTransaction st WHERE st.status = 'PENDING' AND st.transactionTime < :beforeTime ORDER BY st.transactionTime ASC")
    List<SmartEarnTransaction> findPendingTransactionsBefore(@Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * Find transactions that need confirmation
     */
    @Query("SELECT st FROM SmartEarnTransaction st WHERE st.status = 'PENDING' AND st.confirmationDate <= :currentTime ORDER BY st.transactionTime ASC")
    List<SmartEarnTransaction> findTransactionsReadyForConfirmation(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Get total deposits for an account
     */
    @Query("SELECT COALESCE(SUM(st.amount), 0) FROM SmartEarnTransaction st WHERE st.smartEarnAccount = :account AND st.transactionType IN ('DEPOSIT', 'TRANSFER_IN') AND st.status = 'SUCCESS'")
    BigDecimal getTotalDeposits(@Param("account") SmartEarnAccount account);
    
    /**
     * Get total withdrawals for an account
     */
    @Query("SELECT COALESCE(SUM(st.amount), 0) FROM SmartEarnTransaction st WHERE st.smartEarnAccount = :account AND st.transactionType IN ('WITHDRAWAL', 'TRANSFER_OUT') AND st.status = 'SUCCESS'")
    BigDecimal getTotalWithdrawals(@Param("account") SmartEarnAccount account);
    
    /**
     * Get total processing fees for an account
     */
    @Query("SELECT COALESCE(SUM(st.processingFee), 0) FROM SmartEarnTransaction st WHERE st.smartEarnAccount = :account AND st.status = 'SUCCESS'")
    BigDecimal getTotalProcessingFees(@Param("account") SmartEarnAccount account);
    
    /**
     * Get total interest credited for an account
     */
    @Query("SELECT COALESCE(SUM(st.amount), 0) FROM SmartEarnTransaction st WHERE st.smartEarnAccount = :account AND st.transactionType = 'INTEREST_CREDIT' AND st.status = 'SUCCESS'")
    BigDecimal getTotalInterestCredited(@Param("account") SmartEarnAccount account);
    
    /**
     * Find transactions by date range with pagination
     */
    @Query("SELECT st FROM SmartEarnTransaction st WHERE st.transactionTime BETWEEN :startDate AND :endDate ORDER BY st.transactionTime DESC")
    Page<SmartEarnTransaction> findTransactionsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate,
                                                          Pageable pageable);
    
    /**
     * Count transactions by type and status
     */
    long countByTransactionTypeAndStatus(SmartEarnTransaction.TransactionType transactionType, 
                                       SmartEarnTransaction.TransactionStatus status);
    
    /**
     * Check if reference exists
     */
    boolean existsByReference(String reference);
    
    /**
     * Count transactions by status
     */
    long countByStatus(SmartEarnTransaction.TransactionStatus status);
    
    /**
     * Count transactions by time range
     */
    long countByTransactionTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}
