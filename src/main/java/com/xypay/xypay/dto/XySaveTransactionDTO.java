package com.xypay.xypay.dto;

import com.xypay.xypay.domain.XySaveTransaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class XySaveTransactionDTO {
    
    private UUID id;
    private String transactionType;
    private String transactionTypeDisplay;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String reference;
    private String description;
    private String metadata;
    private LocalDateTime createdAt;
    
    public XySaveTransactionDTO() {}
    
    public XySaveTransactionDTO(XySaveTransaction transaction) {
        this.id = transaction.getId();
        this.transactionType = transaction.getTransactionType().getCode();
        this.transactionTypeDisplay = transaction.getTransactionType().getDescription();
        this.amount = transaction.getAmount();
        this.balanceBefore = transaction.getBalanceBefore();
        this.balanceAfter = transaction.getBalanceAfter();
        this.reference = transaction.getReference();
        this.description = transaction.getDescription();
        this.metadata = transaction.getMetadata();
        this.createdAt = transaction.getCreatedAt();
    }
}
