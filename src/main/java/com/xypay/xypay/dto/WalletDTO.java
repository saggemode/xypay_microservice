package com.xypay.xypay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class WalletDTO {
    private UUID id;
    private UUID userId;
    private UUID branchId;
    private String accountNumber;
    private String alternativeAccountNumber;
    private BigDecimal balance;
    private String currency;
    private String phoneAlias;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
