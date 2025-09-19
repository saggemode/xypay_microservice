package com.xypay.xypay.repository;

import com.xypay.xypay.domain.GeneralLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface GeneralLedgerRepository extends JpaRepository<GeneralLedger, UUID> {
    
    List<GeneralLedger> findByBank_Id(java.util.UUID bankId);
    
    List<GeneralLedger> findByChartOfAccounts_Id(java.util.UUID chartOfAccountsId);
    
    List<GeneralLedger> findByReferenceNumber(String referenceNumber);
    
    List<GeneralLedger> findByBatchNumber(String batchNumber);
    
    List<GeneralLedger> findBySourceTransactionId(Long sourceTransactionId);
    
    List<GeneralLedger> findByCustomerId(Long customerId);
    
    List<GeneralLedger> findByPostingStatus(GeneralLedger.PostingStatus postingStatus);
    
    List<GeneralLedger> findByReversalIndicatorTrue();
    
    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.chartOfAccounts.id = :accountId AND gl.transactionDate BETWEEN :startDate AND :endDate ORDER BY gl.transactionDate")
    List<GeneralLedger> findByAccountAndDateRange(@Param("accountId") java.util.UUID accountId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.bank.id = :bankId AND gl.transactionDate BETWEEN :startDate AND :endDate ORDER BY gl.transactionDate")
    List<GeneralLedger> findByBankAndDateRange(@Param("bankId") java.util.UUID bankId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.branch.id = :branchId AND gl.transactionDate BETWEEN :startDate AND :endDate ORDER BY gl.transactionDate")
    List<GeneralLedger> findByBranchAndDateRange(@Param("branchId") java.util.UUID branchId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(gl.debitAmount) FROM GeneralLedger gl WHERE gl.chartOfAccounts.id = :accountId AND gl.postingStatus = 'POSTED'")
    BigDecimal sumDebitsByAccount(@Param("accountId") java.util.UUID accountId);
    
    @Query("SELECT SUM(gl.creditAmount) FROM GeneralLedger gl WHERE gl.chartOfAccounts.id = :accountId AND gl.postingStatus = 'POSTED'")
    BigDecimal sumCreditsByAccount(@Param("accountId") java.util.UUID accountId);
    
    @Query("SELECT SUM(gl.debitAmount) - SUM(gl.creditAmount) FROM GeneralLedger gl WHERE gl.chartOfAccounts.id = :accountId AND gl.postingStatus = 'POSTED'")
    BigDecimal getAccountBalance(@Param("accountId") java.util.UUID accountId);
    
    @Query("SELECT SUM(gl.debitAmount) FROM GeneralLedger gl WHERE gl.chartOfAccounts.accountType = :accountType AND gl.bank.id = :bankId AND gl.postingStatus = 'POSTED'")
    BigDecimal sumDebitsByAccountType(@Param("bankId") java.util.UUID bankId, @Param("accountType") com.xypay.xypay.domain.ChartOfAccounts.AccountType accountType);
    
    @Query("SELECT SUM(gl.creditAmount) FROM GeneralLedger gl WHERE gl.chartOfAccounts.accountType = :accountType AND gl.bank.id = :bankId AND gl.postingStatus = 'POSTED'")
    BigDecimal sumCreditsByAccountType(@Param("bankId") java.util.UUID bankId, @Param("accountType") com.xypay.xypay.domain.ChartOfAccounts.AccountType accountType);
    
    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.sourceModule = :sourceModule AND gl.bank.id = :bankId ORDER BY gl.transactionDate DESC")
    List<GeneralLedger> findByBankAndSourceModule(@Param("bankId") java.util.UUID bankId, @Param("sourceModule") String sourceModule);
    
    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.currencyCode = :currencyCode AND gl.bank.id = :bankId ORDER BY gl.transactionDate DESC")
    List<GeneralLedger> findByBankAndCurrency(@Param("bankId") java.util.UUID bankId, @Param("currencyCode") String currencyCode);
    
    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.ifrsStage = :ifrsStage AND gl.bank.id = :bankId")
    List<GeneralLedger> findByBankAndIFRSStage(@Param("bankId") java.util.UUID bankId, @Param("ifrsStage") GeneralLedger.IFRSStage ifrsStage);
    
    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.makerCheckerStatus = :status AND gl.bank.id = :bankId")
    List<GeneralLedger> findByBankAndMakerCheckerStatus(@Param("bankId") java.util.UUID bankId, @Param("status") GeneralLedger.MakerCheckerStatus status);
    
    @Query("SELECT COUNT(gl) FROM GeneralLedger gl WHERE gl.bank.id = :bankId AND gl.transactionDate BETWEEN :startDate AND :endDate")
    Long countTransactionsByBankAndDateRange(@Param("bankId") java.util.UUID bankId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT DISTINCT gl.sourceModule FROM GeneralLedger gl WHERE gl.bank.id = :bankId")
    List<String> findDistinctSourceModulesByBank(@Param("bankId") java.util.UUID bankId);
    
    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.description LIKE CONCAT('%', :searchTerm, '%') AND gl.bank.id = :bankId ORDER BY gl.transactionDate DESC")
    List<GeneralLedger> searchByDescription(@Param("bankId") java.util.UUID bankId, @Param("searchTerm") String searchTerm);
}
