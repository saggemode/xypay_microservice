package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "charge_history", indexes = {
    @Index(name = "idx_charge_type_effective", columnList = "charge_type, effective_from"),
    @Index(name = "idx_charge_created", columnList = "created_at")
})
public class ChargeHistory extends BaseEntity {
    
    public enum ChargeType {
        FEE("fee", "Transfer Fee"),
        VAT("vat", "VAT"),
        LEVY("levy", "CBN Levy");
        
        private final String code;
        private final String displayName;
        
        ChargeType(String code, String displayName) {
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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "charge_type", length = 20, nullable = false)
    private ChargeType chargeType;
    
    // Previous and new values
    @Column(name = "old_value", precision = 10, scale = 4, nullable = false)
    private BigDecimal oldValue;
    
    @Column(name = "new_value", precision = 10, scale = 4, nullable = false)
    private BigDecimal newValue;
    
    // Change metadata
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id")
    private User changedBy;
    
    @Column(name = "change_reason", columnDefinition = "TEXT", nullable = false)
    private String changeReason;
    
    @Column(name = "approval_reference", length = 100)
    private String approvalReference;
    
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;
    
    // Constructors
    public ChargeHistory() {}
    
    public ChargeHistory(ChargeType chargeType, BigDecimal oldValue, BigDecimal newValue, 
                        User changedBy, String changeReason, LocalDateTime effectiveFrom) {
        this.chargeType = chargeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.changeReason = changeReason;
        this.effectiveFrom = effectiveFrom;
    }
}
