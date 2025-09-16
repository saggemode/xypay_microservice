package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "vat_charges", indexes = {
    @Index(name = "idx_vat_active", columnList = "active"),
    @Index(name = "idx_vat_effective", columnList = "effective_from, effective_to")
})
public class VATCharge extends BaseEntity {
    
    @Column(name = "rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal rate;
    
    @Column(name = "active", nullable = false)
    private Boolean active = true;
    
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;
    
    // Additional Configuration
    @Column(name = "applies_to_fees", nullable = false)
    private Boolean appliesToFees = true;
    
    @Column(name = "applies_to_levies", nullable = false)
    private Boolean appliesToLevies = false;
    
    @Column(name = "minimum_vatable_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal minimumVatableAmount = BigDecimal.ZERO;
    
    // Exemption Settings
    @Column(name = "exempt_internal_transfers", nullable = false)
    private Boolean exemptInternalTransfers = false;
    
    @Column(name = "exempt_international_transfers", nullable = false)
    private Boolean exemptInternationalTransfers = false;
    
    // Rounding Configuration
    @Column(name = "rounding_method", length = 20, nullable = false)
    private String roundingMethod = "none"; // none, nearest, up, down
    
    // Constructors
    public VATCharge() {}
    
    public VATCharge(BigDecimal rate, Boolean active, LocalDateTime effectiveFrom) {
        this.rate = rate;
        this.active = active;
        this.effectiveFrom = effectiveFrom;
    }
    
    // Business methods
    public BigDecimal calculateVat(BigDecimal baseAmount, String transferType) {
        if (!active) {
            return BigDecimal.ZERO;
        }
        
        if (baseAmount.compareTo(minimumVatableAmount) < 0) {
            return BigDecimal.ZERO;
        }
        
        if (exemptInternalTransfers && "internal".equals(transferType)) {
            return BigDecimal.ZERO;
        }
        
        if (exemptInternationalTransfers && "international".equals(transferType)) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal vatAmount = baseAmount.multiply(rate);
        
        switch (roundingMethod) {
            case "nearest":
                vatAmount = new BigDecimal(Math.round(vatAmount.doubleValue()));
                break;
            case "up":
                vatAmount = new BigDecimal(Math.ceil(vatAmount.doubleValue()));
                break;
            case "down":
                vatAmount = new BigDecimal(Math.floor(vatAmount.doubleValue()));
                break;
            // "none" - no rounding
        }
        
        return vatAmount;
    }
    
    @Override
    public String toString() {
        double percentage = rate != null ? rate.doubleValue() * 100 : 0;
        return String.format("VAT %.2f%% (%s)", percentage, active ? "Active" : "Inactive");
    }
}