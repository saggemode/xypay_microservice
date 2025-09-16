package com.xypay.xypay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FixedSavingsTransactionDTO {
    
    private UUID id;
    private UUID fixedSavingsAccountId;
    private String transactionType;
    private String transactionTypeDisplay;
    private String amount;
    private String balanceBefore;
    private String balanceAfter;
    private String reference;
    private String description;
    private String interestEarned;
    private BigDecimal interestRateApplied;
    private String sourceAccount;
    private String sourceAccountDisplay;
    private String sourceTransactionId;
    private String metadata;
    private LocalDateTime createdAt;
}