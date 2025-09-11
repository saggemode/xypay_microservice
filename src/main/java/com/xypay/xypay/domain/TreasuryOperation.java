package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "treasury_operations")
public class TreasuryOperation extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;
    
    @Column(name = "operation_number", length = 50, unique = true, nullable = false)
    private String operationNumber;
    
    @Column(name = "operation_type")
    @Enumerated(EnumType.STRING)
    private OperationType operationType;
    
    @Column(name = "instrument_type")
    @Enumerated(EnumType.STRING)
    private InstrumentType instrumentType;
    
    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    @Column(name = "base_currency_amount", precision = 19, scale = 2)
    private BigDecimal baseCurrencyAmount;
    
    @Column(name = "exchange_rate", precision = 19, scale = 6)
    private BigDecimal exchangeRate;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OperationStatus status = OperationStatus.PENDING;
    
    // Money Market Operations
    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate;
    
    @Column(name = "maturity_date")
    private LocalDateTime maturityDate;
    
    @Column(name = "value_date")
    private LocalDateTime valueDate;
    
    @Column(name = "settlement_date")
    private LocalDateTime settlementDate;
    
    @Column(name = "counterparty", length = 200)
    private String counterparty;
    
    @Column(name = "counterparty_rating")
    @Enumerated(EnumType.STRING)
    private CreditRating counterpartyRating = CreditRating.BBB;
    
    // Foreign Exchange
    @Column(name = "base_currency", length = 3)
    private String baseCurrency;
    
    @Column(name = "quote_currency", length = 3)
    private String quoteCurrency;
    
    @Column(name = "spot_rate", precision = 19, scale = 6)
    private BigDecimal spotRate;
    
    @Column(name = "forward_rate", precision = 19, scale = 6)
    private BigDecimal forwardRate;
    
    @Column(name = "forward_points", precision = 19, scale = 6)
    private BigDecimal forwardPoints;
    
    // Derivatives
    @Column(name = "underlying_asset", length = 100)
    private String underlyingAsset;
    
    @Column(name = "strike_price", precision = 19, scale = 6)
    private BigDecimal strikePrice;
    
    @Column(name = "premium", precision = 19, scale = 2)
    private BigDecimal premium;
    
    @Column(name = "option_type")
    @Enumerated(EnumType.STRING)
    private OptionType optionType;
    
    @Column(name = "exercise_style")
    @Enumerated(EnumType.STRING)
    private ExerciseStyle exerciseStyle = ExerciseStyle.EUROPEAN;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    // Risk Management
    @Column(name = "var_amount", precision = 19, scale = 2)
    private BigDecimal varAmount; // Value at Risk
    
    @Column(name = "duration", precision = 10, scale = 4)
    private BigDecimal duration;
    
    @Column(name = "convexity", precision = 10, scale = 4)
    private BigDecimal convexity;
    
    @Column(name = "delta", precision = 10, scale = 6)
    private BigDecimal delta;
    
    @Column(name = "gamma", precision = 10, scale = 6)
    private BigDecimal gamma;
    
    @Column(name = "theta", precision = 10, scale = 6)
    private BigDecimal theta;
    
    @Column(name = "vega", precision = 10, scale = 6)
    private BigDecimal vega;
    
    @Column(name = "rho", precision = 10, scale = 6)
    private BigDecimal rho;
    
    // Regulatory and Compliance
    @Column(name = "regulatory_capital", precision = 19, scale = 2)
    private BigDecimal regulatoryCapital;
    
    @Column(name = "risk_weight", precision = 5, scale = 2)
    private BigDecimal riskWeight;
    
    @Column(name = "basel_classification")
    @Enumerated(EnumType.STRING)
    private BaselClassification baselClassification = BaselClassification.TRADING_BOOK;
    
    @Column(name = "ifrs_classification")
    @Enumerated(EnumType.STRING)
    private IfrsClassification ifrsClassification = IfrsClassification.FAIR_VALUE_PL;
    
    // Trading and Settlement
    @Column(name = "trader_id")
    private Long traderId;
    
    @Column(name = "trade_date")
    private LocalDateTime tradeDate;
    
    @Column(name = "booking_date")
    private LocalDateTime bookingDate;
    
    @Column(name = "settlement_instructions", length = 500)
    private String settlementInstructions;
    
    @Column(name = "nostro_account", length = 50)
    private String nostroAccount;
    
    @Column(name = "custodian", length = 200)
    private String custodian;
    
    // Pricing and Valuation
    @Column(name = "market_value", precision = 19, scale = 2)
    private BigDecimal marketValue;
    
    @Column(name = "book_value", precision = 19, scale = 2)
    private BigDecimal bookValue;
    
    @Column(name = "unrealized_pnl", precision = 19, scale = 2)
    private BigDecimal unrealizedPnl = BigDecimal.ZERO;
    
    @Column(name = "realized_pnl", precision = 19, scale = 2)
    private BigDecimal realizedPnl = BigDecimal.ZERO;
    
    @Column(name = "accrued_interest", precision = 19, scale = 2)
    private BigDecimal accruedInterest = BigDecimal.ZERO;
    
    @Column(name = "last_valuation_date")
    private LocalDateTime lastValuationDate;
    
    // Islamic Banking
    @Column(name = "sharia_compliant")
    private Boolean shariaCompliant = false;
    
    @Column(name = "islamic_structure")
    @Enumerated(EnumType.STRING)
    private IslamicStructure islamicStructure;
    
    @Column(name = "profit_sharing_ratio", precision = 5, scale = 2)
    private BigDecimal profitSharingRatio;
    
    // Workflow and Approval
    @Column(name = "approval_workflow_id")
    private Long approvalWorkflowId;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    // Relationships
    @OneToMany(mappedBy = "treasuryOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TreasuryPosition> positions;
    
    @OneToMany(mappedBy = "treasuryOperation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TreasurySettlement> settlements;
    
    public enum OperationType {
        MONEY_MARKET, FOREIGN_EXCHANGE, DERIVATIVES, BOND_TRADING, 
        REPO, REVERSE_REPO, SWAP, FORWARD, OPTION, FUTURE
    }
    
    public enum InstrumentType {
        CALL_MONEY, TERM_DEPOSIT, CERTIFICATE_OF_DEPOSIT, TREASURY_BILL,
        GOVERNMENT_BOND, CORPORATE_BOND, FX_SPOT, FX_FORWARD, FX_SWAP,
        INTEREST_RATE_SWAP, CURRENCY_SWAP, FRA, OPTION, FUTURE, REPO
    }
    
    public enum OperationStatus {
        PENDING, APPROVED, EXECUTED, SETTLED, CANCELLED, EXPIRED, FAILED
    }
    
    public enum CreditRating {
        AAA, AA, A, BBB, BB, B, CCC, CC, C, D
    }
    
    public enum OptionType {
        CALL, PUT
    }
    
    public enum ExerciseStyle {
        EUROPEAN, AMERICAN, BERMUDAN
    }
    
    public enum BaselClassification {
        TRADING_BOOK, BANKING_BOOK, AFS, HTM, LOANS_ADVANCES
    }
    
    public enum IfrsClassification {
        FAIR_VALUE_PL, FAIR_VALUE_OCI, AMORTIZED_COST
    }
    
    public enum IslamicStructure {
        MURABAHA, IJARA, MUSHARAKA, MUDARABA, SUKUK, WAKALA
    }
}
