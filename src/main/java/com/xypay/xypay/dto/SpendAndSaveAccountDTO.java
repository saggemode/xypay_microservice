package com.xypay.xypay.dto;

import com.xypay.xypay.domain.SpendAndSaveAccount;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SpendAndSaveAccountDTO {
    private UUID id;
    private String accountNumber;
    private BigDecimal balance;
    private Boolean isActive;
    private BigDecimal savingsPercentage;
    private BigDecimal totalInterestEarned;
    private BigDecimal totalSavedFromSpending;
    private Integer totalTransactionsProcessed;
    private LocalDate lastAutoSaveDate;
    private LocalDateTime lastInterestCalculation;
    private String defaultWithdrawalDestination;
    private BigDecimal minTransactionAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public SpendAndSaveAccountDTO(SpendAndSaveAccount account) {
        this.id = account.getId();
        this.accountNumber = account.getAccountNumber();
        this.balance = account.getBalance();
        this.isActive = account.getIsActive();
        this.savingsPercentage = account.getSavingsPercentage();
        this.totalInterestEarned = account.getTotalInterestEarned();
        this.totalSavedFromSpending = account.getTotalSavedFromSpending();
        this.totalTransactionsProcessed = account.getTotalTransactionsProcessed();
        this.lastAutoSaveDate = account.getLastAutoSaveDate();
        this.lastInterestCalculation = account.getLastInterestCalculation();
        this.defaultWithdrawalDestination = account.getDefaultWithdrawalDestination().getCode();
        this.minTransactionAmount = account.getMinTransactionAmount();
        this.createdAt = account.getCreatedAt();
        this.updatedAt = account.getUpdatedAt();
    }
}