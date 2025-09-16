package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "xysave_investments")
public class XySaveInvestment {
    
    public enum InvestmentType {
        TREASURY_BILLS("treasury_bills", "Treasury Bills"),
        MUTUAL_FUNDS("mutual_funds", "Mutual Funds"),
        SHORT_TERM_PLACEMENTS("short_term_placements", "Short-term Placements"),
        GOVERNMENT_BONDS("government_bonds", "Government Bonds");
        
        private final String code;
        private final String description;
        
        InvestmentType(String code, String description) {
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
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xysave_account_id", nullable = false)
    private XySaveAccount xysaveAccount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "investment_type", length = 25, nullable = false)
    private InvestmentType investmentType;
    
    @Column(name = "amount_invested", precision = 19, scale = 4, nullable = false)
    private BigDecimal amountInvested;
    
    @Column(name = "current_value", precision = 19, scale = 4, nullable = false)
    private BigDecimal currentValue;
    
    @Column(name = "expected_return_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal expectedReturnRate; // Annual percentage
    
    @Column(name = "maturity_date")
    private LocalDate maturityDate;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getReturnPercentage() {
        if (amountInvested == null || amountInvested.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return currentValue.subtract(amountInvested)
                .divide(amountInvested, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}