package com.xypay.xypay.dto;

import com.xypay.xypay.enums.GeneralStatusChoices;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionDTO {
    private UUID id;
    private UUID walletId;
    private UUID receiverId;
    private String reference;
    private BigDecimal amount;
    private GeneralStatusChoices type;
    private GeneralStatusChoices channel;
    private String description;
    private LocalDateTime timestamp;
    private GeneralStatusChoices status;
    private BigDecimal balanceAfter;
    private UUID parentId;
    private String currency;
    private String metadata;
    private String idempotencyKey;
    private String direction;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
