package com.xypay.xypay.domain;

import com.xypay.xypay.enums.KYCLevelChoices;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transfer_fee_rules", indexes = {
    @Index(name = "idx_fee_rule_priority", columnList = "priority"),
    @Index(name = "idx_fee_rule_active", columnList = "is_active"),
    @Index(name = "idx_fee_rule_amount_range", columnList = "min_amount, max_amount")
})
public class TransferFeeRule extends BaseEntity {
    
    public enum BankType {
        INTERNAL("internal", "Internal Transfer"),
        EXTERNAL("external", "External Transfer"),
        BOTH("both", "Both Internal and External");
        
        private final String code;
        private final String description;
        
        BankType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "min_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal minAmount = BigDecimal.ZERO;
    
    @Column(name = "max_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal maxAmount = BigDecimal.ZERO;
    
    @Column(name = "fee_percent", precision = 5, scale = 4, nullable = false)
    private BigDecimal feePercent = BigDecimal.ZERO;
    
    @Column(name = "fee_fixed", precision = 19, scale = 4, nullable = false)
    private BigDecimal feeFixed = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "bank_type", length = 20, nullable = false)
    private BankType bankType = BankType.BOTH;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_level", length = 20)
    private KYCLevelChoices kycLevel;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;
    
    // Constructors
    public TransferFeeRule() {}
    
    public TransferFeeRule(String name, BigDecimal minAmount, BigDecimal maxAmount, 
                          BigDecimal feePercent, BigDecimal feeFixed, BankType bankType) {
        this.name = name;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.feePercent = feePercent;
        this.feeFixed = feeFixed;
        this.bankType = bankType;
    }
    
    // Business methods
    public BigDecimal calculateFee(BigDecimal amount) {
        if (amount.compareTo(minAmount) < 0) {
            return BigDecimal.ZERO;
        }
        
        if (maxAmount.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(maxAmount) > 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal percentageFee = amount.multiply(feePercent);
        return percentageFee.add(feeFixed);
    }
    
    public boolean isApplicable(BigDecimal amount, String transferType, KYCLevelChoices kycLevel) {
        if (!isActive) {
            return false;
        }
        
        if (amount.compareTo(minAmount) < 0) {
            return false;
        }
        
        if (maxAmount.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(maxAmount) > 0) {
            return false;
        }
        
        if (bankType != BankType.BOTH && !bankType.getCode().equals(transferType)) {
            return false;
        }
        
        if (this.kycLevel != null && kycLevel != this.kycLevel) {
            return false;
        }
        
        return true;
    }
}
