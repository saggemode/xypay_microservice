package com.xypay.xypay.dto;

import com.xypay.xypay.domain.TargetSavingDeposit;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TargetSavingDepositDTO {
    private UUID id;
    private BigDecimal amount;
    private LocalDateTime depositDate;
    private String notes;
    private String source;
    private LocalDateTime createdAt;
    
    public TargetSavingDepositDTO(TargetSavingDeposit deposit) {
        this.id = deposit.getId();
        this.amount = deposit.getAmount();
        this.depositDate = deposit.getDepositDate();
        this.notes = deposit.getNotes();
        this.source = deposit.getSource().getCode();
        // Note: TargetSavingDeposit doesn't have createdAt field, using depositDate instead
        this.createdAt = deposit.getDepositDate();
    }
}
