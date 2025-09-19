package com.xypay.xypay.repository;

import com.xypay.xypay.domain.SmartEarnAccount;
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
public interface SmartEarnAccountRepository extends JpaRepository<SmartEarnAccount, UUID> {
    
    /**
     * Find SmartEarn account by user ID
     */
    Optional<SmartEarnAccount> findByUserId(UUID userId);
    
    /**
     * Find SmartEarn account by account number
     */
    Optional<SmartEarnAccount> findByAccountNumber(String accountNumber);
    
    /**
     * Find all active SmartEarn accounts
     */
    List<SmartEarnAccount> findByIsActiveTrue();
    
    /**
     * Find SmartEarn accounts with balance greater than specified amount
     */
    @Query("SELECT sa FROM SmartEarnAccount sa WHERE sa.balance > :minBalance AND sa.isActive = true")
    List<SmartEarnAccount> findAccountsWithBalanceGreaterThan(@Param("minBalance") BigDecimal minBalance);
    
    /**
     * Find SmartEarn accounts that need interest calculation
     * (accounts with balance > 0 and last interest calculation before specified date)
     */
    @Query("SELECT sa FROM SmartEarnAccount sa WHERE sa.balance > 0 AND sa.isActive = true AND sa.lastInterestCalculation < :beforeDate")
    List<SmartEarnAccount> findAccountsNeedingInterestCalculation(@Param("beforeDate") LocalDateTime beforeDate);
    
    /**
     * Get total balance across all SmartEarn accounts
     */
    @Query("SELECT COALESCE(SUM(sa.balance), 0) FROM SmartEarnAccount sa WHERE sa.isActive = true")
    BigDecimal getTotalBalance();
    
    /**
     * Get total interest earned across all SmartEarn accounts
     */
    @Query("SELECT COALESCE(SUM(sa.totalInterestEarned), 0) FROM SmartEarnAccount sa WHERE sa.isActive = true")
    BigDecimal getTotalInterestEarned();
    
    /**
     * Count active SmartEarn accounts
     */
    long countByIsActiveTrue();
    
    /**
     * Find SmartEarn accounts by user
     */
    List<SmartEarnAccount> findByUser(User user);
    
    /**
     * Check if account number exists
     */
    boolean existsByAccountNumber(String accountNumber);
    
    /**
     * Find SmartEarn accounts created between dates
     */
    @Query("SELECT sa FROM SmartEarnAccount sa WHERE sa.createdAt BETWEEN :startDate AND :endDate AND sa.isActive = true")
    List<SmartEarnAccount> findAccountsCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);
}
