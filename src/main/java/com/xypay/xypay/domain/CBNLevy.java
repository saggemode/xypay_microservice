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
@Table(name = "cbn_levies")
public class CBNLevy extends BaseEntity {
    
    @Column(name = "name", length = 100)
    private String name;
    
    @Column(name = "rate", precision = 5, scale = 4)
    private BigDecimal rate;
    
    @Column(name = "fixed_amount", precision = 19, scale = 4)
    private BigDecimal fixedAmount;
    
    @Column(name = "transaction_type", length = 20)
    private String transactionType = "all"; // all, internal, external, international
    
    @Column(name = "min_amount", precision = 19, scale = 4)
    private BigDecimal minAmount = BigDecimal.ZERO;
    
    @Column(name = "max_amount", precision = 19, scale = 4)
    private BigDecimal maxAmount;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;
    
    @Column(name = "regulation_reference", length = 100)
    private String regulationReference;
    
    @Column(name = "description")
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public CBNLevy() {}
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BigDecimal getRate() {
        return rate;
    }
    
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
    
    public BigDecimal getFixedAmount() {
        return fixedAmount;
    }
    
    public void setFixedAmount(BigDecimal fixedAmount) {
        this.fixedAmount = fixedAmount;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public BigDecimal getMinAmount() {
        return minAmount;
    }
    
    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }
    
    public BigDecimal getMaxAmount() {
        return maxAmount;
    }
    
    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }
    
    public Boolean getActive() {
        return isActive;
    }
    
    public void setActive(Boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }
    
    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }
    
    public LocalDateTime getEffectiveTo() {
        return effectiveTo;
    }
    
    public void setEffectiveTo(LocalDateTime effectiveTo) {
        this.effectiveTo = effectiveTo;
    }
    
    public String getRegulationReference() {
        return regulationReference;
    }
    
    public void setRegulationReference(String regulationReference) {
        this.regulationReference = regulationReference;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
        return String.format("%s - %.2f%% (%s)", name, rate != null ? rate.doubleValue() * 100 : 0, transactionType);
    }
}