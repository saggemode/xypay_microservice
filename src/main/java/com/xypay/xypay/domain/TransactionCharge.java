package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transaction_charges")
public class TransactionCharge extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id")
    private BankTransfer transfer;
    
    @Column(name = "transfer_fee", precision = 19, scale = 4)
    private BigDecimal transferFee = BigDecimal.ZERO;
    
    @Column(name = "vat_amount", precision = 19, scale = 4)
    private BigDecimal vatAmount = BigDecimal.ZERO;
    
    @Column(name = "levy_amount", precision = 19, scale = 4)
    private BigDecimal levyAmount = BigDecimal.ZERO;
    
    @Column(name = "charge_status", length = 20)
    private String chargeStatus = "calculated"; // calculated, applied, failed
    
    @Column(name = "metadata")
    private String metadata;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public TransactionCharge() {}
    
    // Getters and Setters
    public BankTransfer getTransfer() {
        return transfer;
    }
    
    public void setTransfer(BankTransfer transfer) {
        this.transfer = transfer;
    }
    
    public BigDecimal getTransferFee() {
        return transferFee;
    }
    
    public void setTransferFee(BigDecimal transferFee) {
        this.transferFee = transferFee;
    }
    
    public BigDecimal getVatAmount() {
        return vatAmount;
    }
    
    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }
    
    public BigDecimal getLevyAmount() {
        return levyAmount;
    }
    
    public void setLevyAmount(BigDecimal levyAmount) {
        this.levyAmount = levyAmount;
    }
    
    public String getChargeStatus() {
        return chargeStatus;
    }
    
    public void setChargeStatus(String chargeStatus) {
        this.chargeStatus = chargeStatus;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
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
        return String.format("Charges for %s", transfer != null ? transfer.getReference() : "Unknown Transfer");
    }
    
    public BigDecimal getTotalCharges() {
        return transferFee.add(vatAmount).add(levyAmount);
    }
}