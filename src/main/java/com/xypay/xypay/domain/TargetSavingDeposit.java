package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "target_saving_deposits")
public class TargetSavingDeposit extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_saving_id")
    private TargetSaving targetSaving;
    
    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "source", length = 10)
    private String source = "wallet"; // wallet, xysave, both
    
    @Column(name = "deposit_date")
    private LocalDateTime depositDate;
    
    @Column(name = "notes")
    private String notes;
    
    // Constructors
    public TargetSavingDeposit() {}
    
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
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public LocalDateTime getDepositDate() {
        return depositDate;
    }
    
    public void setDepositDate(LocalDateTime depositDate) {
        this.depositDate = depositDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}