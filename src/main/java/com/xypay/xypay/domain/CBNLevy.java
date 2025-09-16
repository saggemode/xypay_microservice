package com.xypay.xypay.domain;

import com.xypay.xypay.enums.TransferType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "cbn_levies")
public class CBNLevy extends BaseEntity {
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal rate;
    
    @Column(name = "fixed_amount", precision = 19, scale = 4)
    private BigDecimal fixedAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    private TransferType transactionType = TransferType.EXTERNAL;
    
    @Column(name = "min_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal minAmount = BigDecimal.ZERO;
    
    @Column(name = "max_amount", precision = 19, scale = 4)
    private BigDecimal maxAmount;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;
    
    @Column(name = "regulation_reference", length = 100)
    private String regulationReference;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // Constructors
    public CBNLevy() {}
    
    public CBNLevy(String name, BigDecimal rate, TransferType transactionType, LocalDateTime effectiveFrom) {
        this.name = name;
        this.rate = rate;
        this.transactionType = transactionType;
        this.effectiveFrom = effectiveFrom;
    }
    
    // Business methods
    public BigDecimal calculateLevy(BigDecimal amount, String transactionType) {
        if (!isActive) {
            return BigDecimal.ZERO;
        }
        
        if (amount.compareTo(minAmount) < 0) {
            return BigDecimal.ZERO;
        }
        
        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            return BigDecimal.ZERO;
        }
        
        if (!this.transactionType.getValue().equals("all") && !this.transactionType.getValue().equals(transactionType)) {
            return BigDecimal.ZERO;
        }
        
        if (fixedAmount != null) {
            return fixedAmount;
        }
        
        return amount.multiply(rate);
    }
    
    @Override
    public String toString() {
        return String.format("%s - %.2f%% (%s)", name, rate != null ? rate.doubleValue() * 100 : 0, transactionType.getValue());
    }
}