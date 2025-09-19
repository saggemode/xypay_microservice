package com.xypay.xypay.config;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.xypay.xypay.domain.BaseEntity;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "charge_configurations")
public class ChargeConfiguration extends BaseEntity {
    
    @Column(name = "charge_name")
    private String chargeName;
    
    @Column(name = "charge_type")
    private String chargeType; // FIXED, PERCENTAGE
    
    @Column(name = "charge_amount")
    private BigDecimal chargeAmount;
    
    @Column(name = "percentage")
    private BigDecimal percentage;
    
    @Column(name = "amount")
    private BigDecimal amount;
    
    @Column(name = "minimum_charge")
    private BigDecimal minimumCharge;
    
    @Column(name = "minimum_amount")
    private BigDecimal minimumAmount;
    
    @Column(name = "maximum_charge")
    private BigDecimal maximumCharge;
    
    @Column(name = "maximum_amount")
    private BigDecimal maximumAmount;
    
    @Column(name = "applicable_products")
    private String applicableProducts; // JSON format list of applicable products
    
    @Column(name = "waiver_conditions")
    private String waiverConditions; // JSON format for waiver conditions
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Explicit getter and setter for isActive field
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    // Getters and setters for new fields
    public BigDecimal getPercentage() {
        return percentage;
    }
    
    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public BigDecimal getMinimumAmount() {
        return minimumAmount;
    }
    
    public void setMinimumAmount(BigDecimal minimumAmount) {
        this.minimumAmount = minimumAmount;
    }
    
    public BigDecimal getMaximumAmount() {
        return maximumAmount;
    }
    
    public void setMaximumAmount(BigDecimal maximumAmount) {
        this.maximumAmount = maximumAmount;
    }
}