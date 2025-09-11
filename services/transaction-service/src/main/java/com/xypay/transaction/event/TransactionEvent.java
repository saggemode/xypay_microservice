package com.xypay.transaction.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
    
    private Long transactionId;
    private String accountNumber;
    private String receiverAccountNumber;
    private String reference;
    private BigDecimal amount;
    private String type;
    private String channel;
    private String description;
    private String status;
    private BigDecimal balanceAfter;
    private String currency;
    private String metadata;
    private String idempotencyKey;
    private String direction;
    private LocalDateTime processedAt;
    private LocalDateTime timestamp;
    private String eventType;
    private LocalDateTime eventTimestamp;
    
}
