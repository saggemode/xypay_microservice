package com.xypay.xypay.dto;

import com.xypay.xypay.domain.TargetSavingWithdrawal;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TargetSavingWithdrawalDTO {
    private UUID id;
    private BigDecimal amount;
    private LocalDateTime withdrawalDate;
    private String destination;
    private String notes;
    private LocalDateTime createdAt;
    
    public TargetSavingWithdrawalDTO(TargetSavingWithdrawal withdrawal) {
        this.id = withdrawal.getId();
        this.amount = withdrawal.getAmount();
        this.withdrawalDate = withdrawal.getWithdrawalDate();
        this.destination = withdrawal.getDestination().getCode();
        this.notes = withdrawal.getNotes();
        // Note: TargetSavingWithdrawal doesn't have createdAt field, using withdrawalDate instead
        this.createdAt = withdrawal.getWithdrawalDate();
    }
}
