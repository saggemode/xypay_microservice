package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transfer_charge_controls")
public class TransferChargeControl extends BaseEntity {
    
    // Core charge controls
    @Column(name = "levy_active", nullable = false)
    private Boolean levyActive = true;
    
    @Column(name = "vat_active", nullable = false)
    private Boolean vatActive = true;
    
    @Column(name = "fee_active", nullable = false)
    private Boolean feeActive = true;
    
    // Charge thresholds
    @Column(name = "min_amount_for_charges", precision = 19, scale = 4, nullable = false)
    private BigDecimal minAmountForCharges = BigDecimal.ZERO;
    
    // Exemption settings
    @Column(name = "exempt_internal_transfers", nullable = false)
    private Boolean exemptInternalTransfers = false;
    
    @Column(name = "exempt_first_monthly_transfer", nullable = false)
    private Boolean exemptFirstMonthlyTransfer = false;
    
    // VAT configuration
    @Column(name = "vat_calculation_base", length = 20, nullable = false)
    private String vatCalculationBase = "fee_only"; // fee_only, fee_and_levy, total_amount
    
    // Levy configuration
    @Column(name = "levy_calculation_method", length = 20, nullable = false)
    private String levyCalculationMethod = "fixed"; // fixed, percentage, tiered
    
    // Charge order
    @Column(name = "charge_application_order", length = 20, nullable = false)
    private String chargeApplicationOrder = "fee_vat_levy"; // fee_vat_levy, levy_vat_fee, vat_fee_levy
    
    // Additional settings
    @Column(name = "round_charges", nullable = false)
    private Boolean roundCharges = true;
    
    @Column(name = "allow_charge_overrides", nullable = false)
    private Boolean allowChargeOverrides = false;
    
    // Audit trail
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;
    
    // Constructors
    public TransferChargeControl() {}
    
    public TransferChargeControl(Boolean levyActive, Boolean vatActive, Boolean feeActive) {
        this.levyActive = levyActive;
        this.vatActive = vatActive;
        this.feeActive = feeActive;
    }
    
    // Business methods
    public BigDecimal calculateTotalCharges(BigDecimal amount, String transferType) {
        if (amount.compareTo(minAmountForCharges) < 0) {
            return BigDecimal.ZERO;
        }
        
        if (exemptInternalTransfers && "internal".equals(transferType)) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalCharges = BigDecimal.ZERO;
        
        // Calculate individual charges based on application order
        String[] order = chargeApplicationOrder.split("_");
        for (String chargeType : order) {
            if ("fee".equals(chargeType) && feeActive) {
                BigDecimal fee = calculateFee(amount);
                totalCharges = totalCharges.add(fee);
            } else if ("vat".equals(chargeType) && vatActive) {
                BigDecimal vat = calculateVat(amount, totalCharges);
                totalCharges = totalCharges.add(vat);
            } else if ("levy".equals(chargeType) && levyActive) {
                BigDecimal levy = calculateLevy(amount);
                totalCharges = totalCharges.add(levy);
            }
        }
        
        if (roundCharges) {
            totalCharges = new BigDecimal(Math.round(totalCharges.doubleValue()));
        }
        
        return totalCharges;
    }
    
    private BigDecimal calculateFee(BigDecimal amount) {
        // Implementation would use TransferFeeRule
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateVat(BigDecimal amount, BigDecimal previousCharges) {
        // Implementation would use VATCharge
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateLevy(BigDecimal amount) {
        // Implementation would use levy calculation rules
        return BigDecimal.ZERO;
    }
    
    @Override
    public String toString() {
        return String.format("Levy: %s, VAT: %s, Fee: %s", 
                           levyActive ? "On" : "Off", 
                           vatActive ? "On" : "Off", 
                           feeActive ? "On" : "Off");
    }
}