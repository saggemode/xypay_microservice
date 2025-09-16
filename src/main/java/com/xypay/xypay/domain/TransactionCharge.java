package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transaction_charges")
public class TransactionCharge extends BaseEntity {
    
    public enum ChargeStatus {
        CALCULATED("calculated", "Calculated"),
        APPLIED("applied", "Applied"),
        REFUNDED("refunded", "Refunded"),
        WAIVED("waived", "Waived");
        
        private final String code;
        private final String displayName;
        
        ChargeStatus(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private BankTransfer transfer;
    
    // Individual charges
    @Column(name = "transfer_fee", precision = 19, scale = 4, nullable = false)
    private BigDecimal transferFee = BigDecimal.ZERO;
    
    @Column(name = "vat_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal vatAmount = BigDecimal.ZERO;
    
    @Column(name = "levy_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal levyAmount = BigDecimal.ZERO;
    
    // Rates used
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_rule_id")
    private TransferFeeRule feeRule;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vat_rate_id")
    private VATCharge vatRate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "levy_id")
    private CBNLevy levy;
    
    // Charge metadata
    @Enumerated(EnumType.STRING)
    @Column(name = "charge_status", length = 20, nullable = false)
    private ChargeStatus chargeStatus = ChargeStatus.CALCULATED;
    
    @Column(name = "waiver_reason", columnDefinition = "TEXT")
    private String waiverReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waived_by_id")
    private User waivedBy;
    
    // Audit trail
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;
    
    // Constructors
    public TransactionCharge() {}
    
    public TransactionCharge(BankTransfer transfer, BigDecimal transferFee, BigDecimal vatAmount, BigDecimal levyAmount) {
        this.transfer = transfer;
        this.transferFee = transferFee;
        this.vatAmount = vatAmount;
        this.levyAmount = levyAmount;
    }
    
    // Business methods
    public BigDecimal getTotalCharges() {
        return transferFee.add(vatAmount).add(levyAmount);
    }
    
    public void waiveCharges(User user, String reason) {
        this.chargeStatus = ChargeStatus.WAIVED;
        this.waiverReason = reason;
        this.waivedBy = user;
        this.transferFee = BigDecimal.ZERO;
        this.vatAmount = BigDecimal.ZERO;
        this.levyAmount = BigDecimal.ZERO;
    }
}