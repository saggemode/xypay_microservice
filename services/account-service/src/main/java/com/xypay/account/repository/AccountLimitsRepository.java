package com.xypay.account.repository;

import com.xypay.account.domain.AccountLimits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountLimitsRepository extends JpaRepository<AccountLimits, Long> {
    
    // Find limits by account
    List<AccountLimits> findByAccountIdAndIsActiveTrue(Long accountId);
    
    List<AccountLimits> findByAccountNumberAndIsActiveTrue(String accountNumber);
    
    // Find limits by type
    Optional<AccountLimits> findByAccountIdAndLimitTypeAndIsActiveTrue(Long accountId, String limitType);
    
    Optional<AccountLimits> findByAccountNumberAndLimitTypeAndIsActiveTrue(String accountNumber, String limitType);
    
    // Find limits by period
    List<AccountLimits> findByAccountIdAndLimitPeriodAndIsActiveTrue(Long accountId, String limitPeriod);
    
    List<AccountLimits> findByAccountNumberAndLimitPeriodAndIsActiveTrue(String accountNumber, String limitPeriod);
    
    // Find all limits for an account
    List<AccountLimits> findByAccountId(Long accountId);
    
    List<AccountLimits> findByAccountNumber(String accountNumber);
    
    // Find limits that need reset (daily, weekly, monthly)
    @Query("SELECT al FROM AccountLimits al WHERE al.limitPeriod IN ('DAILY', 'WEEKLY', 'MONTHLY') AND al.isActive = true")
    List<AccountLimits> findLimitsNeedingReset();
    
    // Find expired limits
    @Query("SELECT al FROM AccountLimits al WHERE al.expiryDate < :currentDate AND al.isActive = true")
    List<AccountLimits> findExpiredLimits(@Param("currentDate") LocalDateTime currentDate);
    
    // Find limits that are not yet effective
    @Query("SELECT al FROM AccountLimits al WHERE al.effectiveDate > :currentDate AND al.isActive = true")
    List<AccountLimits> findFutureEffectiveLimits(@Param("currentDate") LocalDateTime currentDate);
    
    // Find limits by multiple criteria
    @Query("SELECT al FROM AccountLimits al WHERE al.accountId = :accountId AND al.limitType = :limitType AND al.limitPeriod = :limitPeriod AND al.isActive = true")
    Optional<AccountLimits> findByAccountIdAndLimitTypeAndLimitPeriodAndIsActiveTrue(
        @Param("accountId") Long accountId, 
        @Param("limitType") String limitType, 
        @Param("limitPeriod") String limitPeriod);
    
    // Find limits that are close to being exceeded
    @Query("SELECT al FROM AccountLimits al WHERE al.accountId = :accountId AND al.isActive = true AND (al.usedAmount / al.limitAmount) >= 0.8")
    List<AccountLimits> findLimitsNearExhaustion(@Param("accountId") Long accountId);
    
    // Find limits that have been exceeded
    @Query("SELECT al FROM AccountLimits al WHERE al.accountId = :accountId AND al.isActive = true AND al.usedAmount > al.limitAmount")
    List<AccountLimits> findExceededLimits(@Param("accountId") Long accountId);
    
    // Find limits by account and multiple types
    @Query("SELECT al FROM AccountLimits al WHERE al.accountId = :accountId AND al.limitType IN :limitTypes AND al.isActive = true")
    List<AccountLimits> findByAccountIdAndLimitTypeInAndIsActiveTrue(@Param("accountId") Long accountId, @Param("limitTypes") List<String> limitTypes);
    
    // Find limits by account number and multiple types
    @Query("SELECT al FROM AccountLimits al WHERE al.accountNumber = :accountNumber AND al.limitType IN :limitTypes AND al.isActive = true")
    List<AccountLimits> findByAccountNumberAndLimitTypeInAndIsActiveTrue(@Param("accountNumber") String accountNumber, @Param("limitTypes") List<String> limitTypes);
}
