package com.xypay.analytics.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Transaction {
    private Long id;
    private Long walletId;
    private Long receiverId;
    private String reference;
    private BigDecimal amount;
    private String type;
    private String channel;
    private String description;
    private String status;
    private BigDecimal balanceAfter;
    private Long parentId;
    private String currency;
    private String metadata;
    private String idempotencyKey;
    private String direction;
    private LocalDateTime processedAt;
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
