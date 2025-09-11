package com.xypay.treasury.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "treasury_positions")
public class TreasuryPosition extends BaseEntity {
    
    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;
    
    @Column(name = "position_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal positionAmount;
    
    @Column(name = "available_amount", precision = 19, scale = 2)
    private BigDecimal availableAmount;
    
    @Column(name = "reserved_amount", precision = 19, scale = 2)
    private BigDecimal reservedAmount;
    
    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;
    
    @Column(name = "maturity_date")
    private LocalDate maturityDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "position_type")
    private PositionType positionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "liquidity_bucket")
    private LiquidityBucket liquidityBucket;
    
    @Column(name = "interest_rate", precision = 8, scale = 6)
    private BigDecimal interestRate;
    
    @Column(name = "cost_center", length = 50)
    private String costCenter;
    
    @Column(name = "profit_center", length = 50)
    private String profitCenter;
    
    @Column(name = "risk_weight", precision = 5, scale = 2)
    private BigDecimal riskWeight = new BigDecimal("100.00");
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "treasuryPosition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TreasuryTransaction> transactions;
    
    public enum PositionType {
        CASH, DEPOSIT, INVESTMENT, LOAN, DERIVATIVE, FOREX
    }
    
    public enum LiquidityBucket {
        INSTANT, OVERNIGHT, WEEKLY, MONTHLY, QUARTERLY, ANNUAL, NON_LIQUID
    }
}
