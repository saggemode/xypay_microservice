package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "target_saving_withdrawals")
public class TargetSavingWithdrawal extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_saving_id")
    private TargetSaving targetSaving;
    
    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "destination", length = 10)
    private String destination = "wallet"; // wallet, xysave
    
    @Column(name = "withdrawal_date")
    private LocalDateTime withdrawalDate;
    
    @Column(name = "notes")
    private String notes;
    
    // Constructors
    public TargetSavingWithdrawal() {}
    
    // Getters and Setters
    public TargetSaving getTargetSaving() {
        return targetSaving;
    }
    
    public void setTargetSaving(TargetSaving targetSaving) {
        this.targetSaving = targetSaving;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public LocalDateTime getWithdrawalDate() {
        return withdrawalDate;
    }
    
    public void setWithdrawalDate(LocalDateTime withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}