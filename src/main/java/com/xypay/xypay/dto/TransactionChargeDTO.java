package com.xypay.xypay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionChargeDTO {
    private UUID id;
    private UUID transferId;
    private BigDecimal transferFee;
    private BigDecimal vatAmount;
    private BigDecimal levyAmount;
    private UUID feeRuleId;
    private UUID vatRateId;
    private UUID levyId;
    private String chargeStatus;
    private String waiverReason;
    private UUID waivedBy;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private BigDecimal totalCharges;
    
    // Nested DTOs
    private BankTransferDTO transfer;
    private UserDTO waivedByUser;
    private UserDTO createdByUser;
    private UserDTO updatedByUser;
}
