package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "portfolios")
public class Portfolio extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;
    
    @Column(name = "portfolio_number", length = 50, unique = true, nullable = false)
    private String portfolioNumber;
    
    @Column(name = "portfolio_name", length = 200, nullable = false)
    private String portfolioName;
    
    @Column(name = "portfolio_type")
    @Enumerated(EnumType.STRING)
    private PortfolioType portfolioType;
    
    @Column(name = "investment_objective")
    @Enumerated(EnumType.STRING)
    private InvestmentObjective investmentObjective;
    
    @Column(name = "risk_profile")
    @Enumerated(EnumType.STRING)
    private RiskProfile riskProfile;
    
    @Column(name = "base_currency", length = 3)
    private String baseCurrency;
    
    @Column(name = "inception_date")
    private LocalDateTime inceptionDate;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PortfolioStatus status = PortfolioStatus.ACTIVE;
    
    // Valuation
    @Column(name = "total_market_value", precision = 19, scale = 2)
    private BigDecimal totalMarketValue = BigDecimal.ZERO;
    
    @Column(name = "total_cost", precision = 19, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;
    
    @Column(name = "cash_balance", precision = 19, scale = 2)
    private BigDecimal cashBalance = BigDecimal.ZERO;
    
    @Column(name = "accrued_income", precision = 19, scale = 2)
    private BigDecimal accruedIncome = BigDecimal.ZERO;
    
    @Column(name = "unrealized_pnl", precision = 19, scale = 2)
    private BigDecimal unrealizedPnl = BigDecimal.ZERO;
    
    @Column(name = "realized_pnl", precision = 19, scale = 2)
    private BigDecimal realizedPnl = BigDecimal.ZERO;
    
    @Column(name = "last_valuation_date")
    private LocalDateTime lastValuationDate;
    
    // Performance Metrics
    @Column(name = "total_return", precision = 10, scale = 6)
    private BigDecimal totalReturn = BigDecimal.ZERO;
    
    @Column(name = "annualized_return", precision = 10, scale = 6)
    private BigDecimal annualizedReturn = BigDecimal.ZERO;
    
    @Column(name = "sharpe_ratio", precision = 10, scale = 6)
    private BigDecimal sharpeRatio = BigDecimal.ZERO;
    
    @Column(name = "beta", precision = 10, scale = 6)
    private BigDecimal beta = BigDecimal.ZERO;
    
    @Column(name = "alpha", precision = 10, scale = 6)
    private BigDecimal alpha = BigDecimal.ZERO;
    
    @Column(name = "volatility", precision = 5, scale = 4)
    private BigDecimal volatility = BigDecimal.ZERO;
    
    @Column(name = "max_drawdown", precision = 5, scale = 4)
    private BigDecimal maxDrawdown = BigDecimal.ZERO;
    
    // Risk Metrics
    @Column(name = "var_1_day", precision = 19, scale = 2)
    private BigDecimal var1Day = BigDecimal.ZERO;
    
    @Column(name = "var_10_day", precision = 19, scale = 2)
    private BigDecimal var10Day = BigDecimal.ZERO;
    
    @Column(name = "expected_shortfall", precision = 19, scale = 2)
    private BigDecimal expectedShortfall = BigDecimal.ZERO;
    
    // Asset Allocation
    @Column(name = "equity_allocation", precision = 5, scale = 2)
    private BigDecimal equityAllocation = BigDecimal.ZERO;
    
    @Column(name = "fixed_income_allocation", precision = 5, scale = 2)
    private BigDecimal fixedIncomeAllocation = BigDecimal.ZERO;
    
    @Column(name = "alternative_allocation", precision = 5, scale = 2)
    private BigDecimal alternativeAllocation = BigDecimal.ZERO;
    
    @Column(name = "cash_allocation", precision = 5, scale = 2)
    private BigDecimal cashAllocation = BigDecimal.ZERO;
    
    // Limits and Controls
    @Column(name = "max_position_size", precision = 5, scale = 2)
    private BigDecimal maxPositionSize = new BigDecimal("10.00"); // 10%
    
    @Column(name = "max_sector_exposure", precision = 5, scale = 2)
    private BigDecimal maxSectorExposure = new BigDecimal("25.00"); // 25%
    
    @Column(name = "max_single_issuer", precision = 5, scale = 2)
    private BigDecimal maxSingleIssuer = new BigDecimal("5.00"); // 5%
    
    @Column(name = "leverage_limit", precision = 5, scale = 2)
    private BigDecimal leverageLimit = BigDecimal.ZERO;
    
    // Regulatory and Compliance
    @Column(name = "regulatory_capital", precision = 19, scale = 2)
    private BigDecimal regulatoryCapital = BigDecimal.ZERO;
    
    @Column(name = "ifrs_classification")
    @Enumerated(EnumType.STRING)
    private IfrsClassification ifrsClassification = IfrsClassification.FAIR_VALUE_PL;
    
    @Column(name = "is_discretionary")
    private Boolean isDiscretionary = false;
    
    @Column(name = "is_advisory")
    private Boolean isAdvisory = true;
    
    // Islamic Banking
    @Column(name = "sharia_compliant")
    private Boolean shariaCompliant = false;
    
    @Column(name = "sharia_board_approved")
    private Boolean shariaBoardApproved = false;
    
    // Management
    @Column(name = "portfolio_manager_id")
    private UUID portfolioManagerId;
    
    @Column(name = "custodian", length = 200)
    private String custodian;
    
    @Column(name = "benchmark", length = 100)
    private String benchmark;
    
    @Column(name = "management_fee", precision = 5, scale = 4)
    private BigDecimal managementFee = BigDecimal.ZERO;
    
    @Column(name = "performance_fee", precision = 5, scale = 4)
    private BigDecimal performanceFee = BigDecimal.ZERO;
    
    // Relationships
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SecurityHolding> holdings;
    
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SecurityTransaction> transactions;
    
    public enum PortfolioType {
        INDIVIDUAL, INSTITUTIONAL, PENSION_FUND, INSURANCE, MUTUAL_FUND, 
        HEDGE_FUND, PRIVATE_WEALTH, CORPORATE_TREASURY, BANK_PROPRIETARY
    }
    
    public enum InvestmentObjective {
        CAPITAL_PRESERVATION, INCOME, GROWTH, BALANCED, SPECULATION
    }
    
    public enum RiskProfile {
        CONSERVATIVE, MODERATELY_CONSERVATIVE, MODERATE, MODERATELY_AGGRESSIVE, AGGRESSIVE
    }
    
    public enum PortfolioStatus {
        ACTIVE, INACTIVE, SUSPENDED, CLOSED, TERMINATED
    }
    
    public enum IfrsClassification {
        AMORTIZED_COST, FVOCI, FAIR_VALUE_PL
    }
}