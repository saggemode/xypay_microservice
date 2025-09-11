package com.xypay.transaction.dto;

import com.xypay.transaction.domain.Transaction;
import com.xypay.transaction.enums.TransactionChannel;
import com.xypay.transaction.enums.TransactionStatus;
import com.xypay.transaction.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    
    private Long id;
    private String accountNumber;
    private String receiverAccountNumber;
    private String reference;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionChannel channel;
    private String description;
    private TransactionStatus status;
    private BigDecimal balanceAfter;
    private Long parentId;
    private String currency;
    private String metadata;
    private String idempotencyKey;
    private String direction;
    private LocalDateTime processedAt;
    private LocalDateTime timestamp;
    
    // Additional response fields
    private String customerId;
    private String customerName;
    private BigDecimal feeAmount;
    private String feeType;
    private String processingTime;
    private String errorMessage;
    private String errorCode;
    
    public static TransactionResponse fromTransaction(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setAccountNumber(transaction.getAccountNumber());
        response.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
        response.setReference(transaction.getReference());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setChannel(transaction.getChannel());
        response.setDescription(transaction.getDescription());
        response.setStatus(transaction.getStatus());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setParentId(transaction.getParentId());
        response.setCurrency(transaction.getCurrency());
        response.setMetadata(transaction.getMetadata());
        response.setIdempotencyKey(transaction.getIdempotencyKey());
        response.setDirection(transaction.getDirection());
        response.setProcessedAt(transaction.getProcessedAt());
        response.setTimestamp(transaction.getTimestamp());
        return response;
    }
}
