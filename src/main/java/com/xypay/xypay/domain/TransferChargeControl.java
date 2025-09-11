package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transfer_charge_controls")
public class TransferChargeControl extends BaseEntity {
    
    @Column(name = "levy_active")
    private Boolean levyActive = true;
    
    @Column(name = "vat_active")
    private Boolean vatActive = true;
    
    @Column(name = "fee_active")
    private Boolean feeActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public TransferChargeControl() {}
    
    public TransferChargeControl(Boolean levyActive, Boolean vatActive, Boolean feeActive) {
        this.levyActive = levyActive;
        this.vatActive = vatActive;
        this.feeActive = feeActive;
    }
    
    // Getters and Setters
    public Boolean getLevyActive() {
        return levyActive;
    }
    
    public void setLevyActive(Boolean levyActive) {
        this.levyActive = levyActive;
    }
    
    public Boolean getVatActive() {
        return vatActive;
    }
    
    public void setVatActive(Boolean vatActive) {
        this.vatActive = vatActive;
    }
    
    public Boolean getFeeActive() {
        return feeActive;
    }
    
    public void setFeeActive(Boolean feeActive) {
        this.feeActive = feeActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "TransferChargeControl{" +
                "levyActive=" + levyActive +
                ", vatActive=" + vatActive +
                ", feeActive=" + feeActive +
                '}';
    }
}