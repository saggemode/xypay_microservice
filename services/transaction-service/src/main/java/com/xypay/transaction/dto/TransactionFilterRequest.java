package com.xypay.transaction.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionFilterRequest {
    
    private Long walletId;
    private String status;
    private String type;
    private String channel;
    private String currency;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String reference;
    private String description;
    private Long parentId;
    private Long receiverId;
    private int page = 0;
    private int size = 20;
    private String sortBy = "timestamp";
    private String sortDirection = "DESC";
}