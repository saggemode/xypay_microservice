package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "security_holdings")
public class SecurityHolding extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_id", nullable = false)
    private Security security;
    
    @Column(name = "quantity", precision = 19, scale = 6, nullable = false)
    private BigDecimal quantity;
    
    @Column(name = "average_cost", precision = 19, scale = 6)
    private BigDecimal averageCost;
    
    @Column(name = "total_cost", precision = 19, scale = 2)
    private BigDecimal totalCost;
    
    @Column(name = "market_value", precision = 19, scale = 2)
    private BigDecimal marketValue;
    
    @Column(name = "unrealized_pnl", precision = 19, scale = 2)
    private BigDecimal unrealizedPnl = BigDecimal.ZERO;
    
    @Column(name = "realized_pnl", precision = 19, scale = 2)
    private BigDecimal realizedPnl = BigDecimal.ZERO;
    
    @Column(name = "accrued_income", precision = 19, scale = 2)
    private BigDecimal accruedIncome = BigDecimal.ZERO;
    
    @Column(name = "last_valuation_date")
    private LocalDateTime lastValuationDate;
    
    @Column(name = "acquisition_date")
    private LocalDateTime acquisitionDate;
    
    @Column(name = "holding_period_days")
    private Integer holdingPeriodDays;
    
    // Risk Metrics
    @Column(name = "position_var", precision = 19, scale = 2)
    private BigDecimal positionVar;
    
    @Column(name = "weight_in_portfolio", precision = 5, scale = 4)
    private BigDecimal weightInPortfolio;
    
    // Regulatory
    @Column(name = "regulatory_capital", precision = 19, scale = 2)
    private BigDecimal regulatoryCapital;
    
    @Column(name = "ifrs_stage")
    @Enumerated(EnumType.STRING)
    private IfrsStage ifrsStage = IfrsStage.STAGE_1;
    
    @Column(name = "is_restricted")
    private Boolean isRestricted = false;
    
    @Column(name = "restriction_reason", length = 200)
    private String restrictionReason;
    
    public enum IfrsStage {
        STAGE_1, STAGE_2, STAGE_3
    }
}
