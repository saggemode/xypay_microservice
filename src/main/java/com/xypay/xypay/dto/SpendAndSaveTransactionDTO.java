package com.xypay.xypay.dto;

import com.xypay.xypay.domain.SpendAndSaveTransaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SpendAndSaveTransactionDTO {
    private UUID id;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String reference;
    private String description;
    private UUID originalTransactionId;
    private BigDecimal originalTransactionAmount;
    private BigDecimal savingsPercentageApplied;
    private String withdrawalDestination;
    private String destinationAccount;
    private BigDecimal interestEarned;
    private String interestBreakdown;
    private LocalDateTime createdAt;
    
    public SpendAndSaveTransactionDTO(SpendAndSaveTransaction transaction) {
        this.id = transaction.getId();
        this.transactionType = transaction.getTransactionType().toString();
        this.amount = transaction.getAmount();
        this.balanceBefore = transaction.getBalanceBefore();
        this.balanceAfter = transaction.getBalanceAfter();
        this.reference = transaction.getReference();
        this.description = transaction.getDescription();
        this.originalTransactionId = transaction.getOriginalTransactionId();
        this.originalTransactionAmount = transaction.getOriginalTransactionAmount();
        this.savingsPercentageApplied = transaction.getSavingsPercentageApplied();
        this.withdrawalDestination = transaction.getWithdrawalDestination() != null ? transaction.getWithdrawalDestination().toString() : null;
        this.destinationAccount = transaction.getDestinationAccount();
        this.interestEarned = transaction.getInterestEarned();
        this.interestBreakdown = transaction.getInterestBreakdown();
        this.createdAt = transaction.getCreatedAt();
    }
}