package com.xypay.xypay.repository;

import com.xypay.xypay.domain.ChartOfAccounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChartOfAccountsRepository extends JpaRepository<ChartOfAccounts, UUID> {
    
    List<ChartOfAccounts> findByBankId(UUID bankId);
    
    List<ChartOfAccounts> findByBankIdAndIsActiveTrue(UUID bankId);
    
    Optional<ChartOfAccounts> findByAccountCode(String accountCode);
    
    Optional<ChartOfAccounts> findByBankIdAndAccountCode(UUID bankId, String accountCode);
    
    List<ChartOfAccounts> findByAccountType(ChartOfAccounts.AccountType accountType);
    
    List<ChartOfAccounts> findByBankIdAndAccountType(UUID bankId, ChartOfAccounts.AccountType accountType);
    
    List<ChartOfAccounts> findByAccountCategory(ChartOfAccounts.AccountCategory accountCategory);
    
    List<ChartOfAccounts> findByBankIdAndAccountCategory(UUID bankId, ChartOfAccounts.AccountCategory accountCategory);
    
    List<ChartOfAccounts> findByParentAccountCode(String parentAccountCode);
    
    List<ChartOfAccounts> findByBankIdAndParentAccountCode(UUID bankId, String parentAccountCode);
    
    List<ChartOfAccounts> findByAccountLevel(Integer accountLevel);
    
    List<ChartOfAccounts> findByBankIdAndAccountLevel(UUID bankId, Integer accountLevel);
    
    List<ChartOfAccounts> findByIsControlAccountTrue();
    
    List<ChartOfAccounts> findByBankIdAndIsControlAccountTrue(UUID bankId);
    
    List<ChartOfAccounts> findByAllowPostingTrue();
    
    List<ChartOfAccounts> findByCurrencyCode(String currencyCode);
    
    List<ChartOfAccounts> findByBankIdAndCurrencyCode(UUID bankId, String currencyCode);
    
    List<ChartOfAccounts> findByStatutoryReturnsTrue();
    
    List<ChartOfAccounts> findByBankIdAndStatutoryReturnsTrue(UUID bankId);
    
    @Query("SELECT c FROM ChartOfAccounts c WHERE c.bank.id = :bankId AND c.accountName LIKE CONCAT('%', :searchTerm, '%')")
    List<ChartOfAccounts> findByBankIdAndAccountNameContaining(@Param("bankId") UUID bankId, @Param("searchTerm") String searchTerm);
    
    @Query("SELECT c FROM ChartOfAccounts c WHERE c.bank.id = :bankId AND (c.accountCode LIKE CONCAT('%', :searchTerm, '%') OR c.accountName LIKE CONCAT('%', :searchTerm, '%'))")
    List<ChartOfAccounts> searchByCodeOrName(@Param("bankId") UUID bankId, @Param("searchTerm") String searchTerm);
    
    @Query("SELECT DISTINCT c.accountLevel FROM ChartOfAccounts c WHERE c.bank.id = :bankId ORDER BY c.accountLevel")
    List<Integer> findDistinctAccountLevelsByBankId(@Param("bankId") UUID bankId);
    
    @Query("SELECT c FROM ChartOfAccounts c WHERE c.bank.id = :bankId AND c.parentAccountCode IS NULL ORDER BY c.accountCode")
    List<ChartOfAccounts> findRootAccountsByBankId(@Param("bankId") UUID bankId);
    
    @Query("SELECT COUNT(c) FROM ChartOfAccounts c WHERE c.bank.id = :bankId AND c.accountType = :accountType")
    Long countByBankIdAndAccountType(@Param("bankId") UUID bankId, @Param("accountType") ChartOfAccounts.AccountType accountType);
}
