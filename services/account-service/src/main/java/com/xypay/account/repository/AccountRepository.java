package com.xypay.account.repository;

import com.xypay.account.domain.Account;
import com.xypay.account.enums.AccountStatus;
import com.xypay.account.enums.AccountType;
import com.xypay.account.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByAccountNumber(String accountNumber);
    
    List<Account> findByCustomerId(Long customerId);
    
    @Query("SELECT a FROM Account a WHERE a.customerId = :customerId AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByCustomerId(@Param("customerId") Long customerId);
    
    List<Account> findByStatus(AccountStatus status);
    
    List<Account> findByAccountType(AccountType accountType);
    
    List<Account> findByBranchId(Long branchId);
    
    List<Account> findByCurrency(Currency currency);
    
    @Query("SELECT a FROM Account a WHERE a.ledgerBalance > :minBalance")
    List<Account> findByMinLedgerBalance(@Param("minBalance") BigDecimal minBalance);
    
    @Query("SELECT a FROM Account a WHERE a.availableBalance > :minBalance")
    List<Account> findByMinAvailableBalance(@Param("minBalance") BigDecimal minBalance);
    
    // Enhanced queries for core banking
    @Query("SELECT a FROM Account a WHERE a.customerId = :customerId AND a.status IN :statuses")
    List<Account> findByCustomerIdAndStatusIn(@Param("customerId") Long customerId, @Param("statuses") List<AccountStatus> statuses);
    
    @Query("SELECT a FROM Account a WHERE a.accountType = :accountType AND a.status = :status")
    List<Account> findByAccountTypeAndStatus(@Param("accountType") AccountType accountType, @Param("status") AccountStatus status);
    
    @Query("SELECT a FROM Account a WHERE a.branchId = :branchId AND a.status = :status")
    List<Account> findByBranchIdAndStatus(@Param("branchId") Long branchId, @Param("status") AccountStatus status);
    
    @Query("SELECT a FROM Account a WHERE a.currency = :currency AND a.status = :status")
    List<Account> findByCurrencyAndStatus(@Param("currency") Currency currency, @Param("status") AccountStatus status);
    
    @Query("SELECT a FROM Account a WHERE a.ledgerBalance BETWEEN :minBalance AND :maxBalance")
    List<Account> findByLedgerBalanceBetween(@Param("minBalance") BigDecimal minBalance, @Param("maxBalance") BigDecimal maxBalance);
    
    @Query("SELECT a FROM Account a WHERE a.availableBalance BETWEEN :minBalance AND :maxBalance")
    List<Account> findByAvailableBalanceBetween(@Param("minBalance") BigDecimal minBalance, @Param("maxBalance") BigDecimal maxBalance);
    
    @Query("SELECT a FROM Account a WHERE a.overdraftUsed > 0")
    List<Account> findOverdrawnAccounts();
    
    @Query("SELECT a FROM Account a WHERE a.overdraftUsed > 0 AND a.status = :status")
    List<Account> findOverdrawnAccountsByStatus(@Param("status") AccountStatus status);
    
    @Query("SELECT a FROM Account a WHERE a.interestRate > 0")
    List<Account> findInterestBearingAccounts();
    
    @Query("SELECT a FROM Account a WHERE a.interestRate > 0 AND a.status = :status")
    List<Account> findInterestBearingAccountsByStatus(@Param("status") AccountStatus status);
    
    @Query("SELECT a FROM Account a WHERE a.lastTransactionDate < :date")
    List<Account> findDormantAccounts(@Param("date") LocalDateTime date);
    
    @Query("SELECT a FROM Account a WHERE a.lastTransactionDate < :date AND a.status = :status")
    List<Account> findDormantAccountsByStatus(@Param("date") LocalDateTime date, @Param("status") AccountStatus status);
    
    @Query("SELECT a FROM Account a WHERE a.ledgerBalance < a.minimumBalance")
    List<Account> findAccountsBelowMinimumBalance();
    
    @Query("SELECT a FROM Account a WHERE a.ledgerBalance < a.minimumBalance AND a.status = :status")
    List<Account> findAccountsBelowMinimumBalanceByStatus(@Param("status") AccountStatus status);
    
    @Query("SELECT a FROM Account a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    List<Account> findAccountsCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Account a WHERE a.lastTransactionDate BETWEEN :startDate AND :endDate")
    List<Account> findAccountsWithTransactionsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Count queries
    @Query("SELECT COUNT(a) FROM Account a WHERE a.customerId = :customerId")
    Long countByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.status = :status")
    Long countByStatus(@Param("status") AccountStatus status);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.accountType = :accountType")
    Long countByAccountType(@Param("accountType") AccountType accountType);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.branchId = :branchId")
    Long countByBranchId(@Param("branchId") Long branchId);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.currency = :currency")
    Long countByCurrency(@Param("currency") Currency currency);
    
    // Sum queries
    @Query("SELECT SUM(a.ledgerBalance) FROM Account a WHERE a.customerId = :customerId")
    Optional<BigDecimal> sumLedgerBalanceByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT SUM(a.availableBalance) FROM Account a WHERE a.customerId = :customerId")
    Optional<BigDecimal> sumAvailableBalanceByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT SUM(a.ledgerBalance) FROM Account a WHERE a.status = :status")
    Optional<BigDecimal> sumLedgerBalanceByStatus(@Param("status") AccountStatus status);
    
    @Query("SELECT SUM(a.availableBalance) FROM Account a WHERE a.status = :status")
    Optional<BigDecimal> sumAvailableBalanceByStatus(@Param("status") AccountStatus status);
    
    @Query("SELECT SUM(a.ledgerBalance) FROM Account a WHERE a.accountType = :accountType")
    Optional<BigDecimal> sumLedgerBalanceByAccountType(@Param("accountType") AccountType accountType);
    
    @Query("SELECT SUM(a.availableBalance) FROM Account a WHERE a.accountType = :accountType")
    Optional<BigDecimal> sumAvailableBalanceByAccountType(@Param("accountType") AccountType accountType);
    
    @Query("SELECT SUM(a.ledgerBalance) FROM Account a WHERE a.branchId = :branchId")
    Optional<BigDecimal> sumLedgerBalanceByBranchId(@Param("branchId") Long branchId);
    
    @Query("SELECT SUM(a.availableBalance) FROM Account a WHERE a.branchId = :branchId")
    Optional<BigDecimal> sumAvailableBalanceByBranchId(@Param("branchId") Long branchId);
    
    @Query("SELECT SUM(a.ledgerBalance) FROM Account a WHERE a.currency = :currency")
    Optional<BigDecimal> sumLedgerBalanceByCurrency(@Param("currency") Currency currency);
    
    @Query("SELECT SUM(a.availableBalance) FROM Account a WHERE a.currency = :currency")
    Optional<BigDecimal> sumAvailableBalanceByCurrency(@Param("currency") Currency currency);
}
