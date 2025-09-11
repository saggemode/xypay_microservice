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
@Table(name = "securities")
public class Security extends BaseEntity {
    
    @Column(name = "symbol", length = 20, unique = true, nullable = false)
    private String symbol;
    
    @Column(name = "isin", length = 12, unique = true)
    private String isin;
    
    @Column(name = "cusip", length = 9)
    private String cusip;
    
    @Column(name = "security_name", length = 200, nullable = false)
    private String securityName;
    
    @Column(name = "security_type")
    @Enumerated(EnumType.STRING)
    private SecurityType securityType;
    
    @Column(name = "asset_class")
    @Enumerated(EnumType.STRING)
    private AssetClass assetClass;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    @Column(name = "exchange", length = 50)
    private String exchange;
    
    @Column(name = "country_code", length = 2)
    private String countryCode;
    
    @Column(name = "sector", length = 100)
    private String sector;
    
    @Column(name = "industry", length = 100)
    private String industry;
    
    // Pricing Information
    @Column(name = "current_price", precision = 19, scale = 6)
    private BigDecimal currentPrice;
    
    @Column(name = "previous_close", precision = 19, scale = 6)
    private BigDecimal previousClose;
    
    @Column(name = "day_high", precision = 19, scale = 6)
    private BigDecimal dayHigh;
    
    @Column(name = "day_low", precision = 19, scale = 6)
    private BigDecimal dayLow;
    
    @Column(name = "bid_price", precision = 19, scale = 6)
    private BigDecimal bidPrice;
    
    @Column(name = "ask_price", precision = 19, scale = 6)
    private BigDecimal askPrice;
    
    @Column(name = "bid_size", precision = 19, scale = 2)
    private BigDecimal bidSize;
    
    @Column(name = "ask_size", precision = 19, scale = 2)
    private BigDecimal askSize;
    
    @Column(name = "volume", precision = 19, scale = 2)
    private BigDecimal volume;
    
    @Column(name = "market_cap", precision = 19, scale = 2)
    private BigDecimal marketCap;
    
    @Column(name = "shares_outstanding", precision = 19, scale = 2)
    private BigDecimal sharesOutstanding;
    
    @Column(name = "last_price_update")
    private LocalDateTime lastPriceUpdate;
    
    // Fixed Income Specific
    @Column(name = "face_value", precision = 19, scale = 2)
    private BigDecimal faceValue;
    
    @Column(name = "coupon_rate", precision = 5, scale = 4)
    private BigDecimal couponRate;
    
    @Column(name = "maturity_date")
    private LocalDateTime maturityDate;
    
    @Column(name = "issue_date")
    private LocalDateTime issueDate;
    
    @Column(name = "yield_to_maturity", precision = 5, scale = 4)
    private BigDecimal yieldToMaturity;
    
    @Column(name = "duration", precision = 10, scale = 4)
    private BigDecimal duration;
    
    @Column(name = "convexity", precision = 10, scale = 4)
    private BigDecimal convexity;
    
    @Column(name = "credit_rating")
    @Enumerated(EnumType.STRING)
    private CreditRating creditRating;
    
    @Column(name = "issuer", length = 200)
    private String issuer;
    
    // Risk Metrics
    @Column(name = "beta", precision = 10, scale = 6)
    private BigDecimal beta;
    
    @Column(name = "volatility", precision = 5, scale = 4)
    private BigDecimal volatility;
    
    @Column(name = "var_1_day", precision = 19, scale = 2)
    private BigDecimal var1Day;
    
    @Column(name = "var_10_day", precision = 19, scale = 2)
    private BigDecimal var10Day;
    
    // Regulatory and Compliance
    @Column(name = "basel_risk_weight", precision = 5, scale = 2)
    private BigDecimal baselRiskWeight;
    
    @Column(name = "ifrs_classification")
    @Enumerated(EnumType.STRING)
    private IfrsClassification ifrsClassification;
    
    @Column(name = "is_liquid")
    private Boolean isLiquid = true;
    
    @Column(name = "is_tradeable")
    private Boolean isTradeable = true;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Islamic Banking
    @Column(name = "sharia_compliant")
    private Boolean shariaCompliant = false;
    
    @Column(name = "sukuk_structure")
    @Enumerated(EnumType.STRING)
    private SukukStructure sukukStructure;
    
    // Relationships
    @OneToMany(mappedBy = "security", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SecurityHolding> holdings;
    
    @OneToMany(mappedBy = "security", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SecurityTransaction> transactions;
    
    public enum SecurityType {
        EQUITY, BOND, PREFERRED_STOCK, ETF, MUTUAL_FUND, OPTION, FUTURE, 
        WARRANT, CONVERTIBLE_BOND, SUKUK, COMMODITY, CURRENCY, DERIVATIVE
    }
    
    public enum AssetClass {
        EQUITY, FIXED_INCOME, COMMODITY, CURRENCY, ALTERNATIVE, REAL_ESTATE, 
        CASH_EQUIVALENT, DERIVATIVE
    }
    
    public enum CreditRating {
        AAA, AA_PLUS, AA, AA_MINUS, A_PLUS, A, A_MINUS, 
        BBB_PLUS, BBB, BBB_MINUS, BB_PLUS, BB, BB_MINUS,
        B_PLUS, B, B_MINUS, CCC_PLUS, CCC, CCC_MINUS, CC, C, D
    }
    
    public enum IfrsClassification {
        FAIR_VALUE_PL, FAIR_VALUE_OCI, AMORTIZED_COST
    }
    
    public enum SukukStructure {
        IJARA, MURABAHA, MUSHARAKA, MUDARABA, WAKALA, ISTISNA
    }
}
