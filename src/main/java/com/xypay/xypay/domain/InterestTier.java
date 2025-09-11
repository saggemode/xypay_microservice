package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "interest_tiers")
public class InterestTier extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_rule_id", nullable = false)
    private InterestRule interestRule;
    
    @Column(name = "tier_sequence", nullable = false)
    private Integer tierSequence;
    
    @Column(name = "tier_name", length = 100)
    private String tierName;
    
    @Column(name = "minimum_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal minimumAmount;
    
    @Column(name = "maximum_amount", precision = 19, scale = 2)
    private BigDecimal maximumAmount;
    
    @Column(name = "interest_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal interestRate;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Helper methods
    public boolean isAmountInTier(BigDecimal amount) {
        if (amount.compareTo(minimumAmount) < 0) {
            return false;
        }
        if (maximumAmount != null && amount.compareTo(maximumAmount) > 0) {
            return false;
        }
        return true;
    }
    
    public BigDecimal getTierAmount(BigDecimal totalAmount) {
        if (!isAmountInTier(totalAmount)) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal tierMin = minimumAmount;
        BigDecimal tierMax = maximumAmount != null ? maximumAmount : totalAmount;
        
        if (totalAmount.compareTo(tierMax) <= 0) {
            return totalAmount.subtract(tierMin);
        } else {
            return tierMax.subtract(tierMin);
        }
    }
}
