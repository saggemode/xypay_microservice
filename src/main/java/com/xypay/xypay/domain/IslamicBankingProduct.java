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
@Table(name = "islamic_banking_products")
public class IslamicBankingProduct extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
    
    @Column(name = "product_code", length = 50, unique = true, nullable = false)
    private String productCode;
    
    @Column(name = "product_name", length = 200, nullable = false)
    private String productName;
    
    @Column(name = "product_category")
    @Enumerated(EnumType.STRING)
    private ProductCategory productCategory;
    
    @Column(name = "islamic_structure")
    @Enumerated(EnumType.STRING)
    private IslamicStructure islamicStructure;
    
    @Column(name = "sharia_board_approved")
    private Boolean shariaBoardApproved = false;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column(name = "approval_reference", length = 100)
    private String approvalReference;
    
    @Column(name = "sharia_advisor", length = 200)
    private String shariaAdvisor;
    
    // Product Details
    @Column(name = "minimum_amount", precision = 19, scale = 2)
    private BigDecimal minimumAmount;
    
    @Column(name = "maximum_amount", precision = 19, scale = 2)
    private BigDecimal maximumAmount;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    @Column(name = "profit_sharing_ratio", precision = 5, scale = 2)
    private BigDecimal profitSharingRatio; // Customer's share percentage
    
    @Column(name = "bank_sharing_ratio", precision = 5, scale = 2)
    private BigDecimal bankSharingRatio; // Bank's share percentage
    
    @Column(name = "expected_profit_rate", precision = 5, scale = 4)
    private BigDecimal expectedProfitRate;
    
    @Column(name = "benchmark_rate", length = 50)
    private String benchmarkRate; // e.g., "KIBOR", "LIBOR"
    
    @Column(name = "profit_calculation_method")
    @Enumerated(EnumType.STRING)
    private ProfitCalculationMethod profitCalculationMethod;
    
    // Term and Maturity
    @Column(name = "minimum_term_months")
    private Integer minimumTermMonths;
    
    @Column(name = "maximum_term_months")
    private Integer maximumTermMonths;
    
    @Column(name = "payment_frequency")
    @Enumerated(EnumType.STRING)
    private PaymentFrequency paymentFrequency;
    
    // Fees and Charges (Sharia-compliant)
    @Column(name = "arrangement_fee", precision = 19, scale = 2)
    private BigDecimal arrangementFee = BigDecimal.ZERO;
    
    @Column(name = "management_fee_rate", precision = 5, scale = 4)
    private BigDecimal managementFeeRate = BigDecimal.ZERO;
    
    @Column(name = "early_settlement_penalty", precision = 5, scale = 2)
    private BigDecimal earlySettlementPenalty = BigDecimal.ZERO;
    
    @Column(name = "late_payment_charity", precision = 19, scale = 2)
    private BigDecimal latePaymentCharity = BigDecimal.ZERO; // Goes to charity, not bank profit
    
    // Eligibility and Requirements
    @Column(name = "minimum_age")
    private Integer minimumAge = 18;
    
    @Column(name = "maximum_age")
    private Integer maximumAge = 65;
    
    @Column(name = "minimum_income", precision = 19, scale = 2)
    private BigDecimal minimumIncome;
    
    @Column(name = "requires_collateral")
    private Boolean requiresCollateral = false;
    
    @Column(name = "collateral_type", length = 100)
    private String collateralType;
    
    @Column(name = "ltv_ratio", precision = 5, scale = 2)
    private BigDecimal ltvRatio; // Loan-to-Value ratio
    
    // Risk and Compliance
    @Column(name = "risk_category")
    @Enumerated(EnumType.STRING)
    private RiskCategory riskCategory = RiskCategory.MEDIUM;
    
    @Column(name = "basel_risk_weight", precision = 5, scale = 2)
    private BigDecimal baselRiskWeight = new BigDecimal("100.00");
    
    @Column(name = "provisioning_rate", precision = 5, scale = 4)
    private BigDecimal provisioningRate = new BigDecimal("1.00");
    
    @Column(name = "regulatory_capital_ratio", precision = 5, scale = 4)
    private BigDecimal regulatoryCapitalRatio = new BigDecimal("8.00");
    
    // Product Status
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "launch_date")
    private LocalDateTime launchDate;
    
    @Column(name = "discontinue_date")
    private LocalDateTime discontinueDate;
    
    @Column(name = "target_market", length = 200)
    private String targetMarket;
    
    // Accounting Treatment
    @Column(name = "ifrs_classification")
    @Enumerated(EnumType.STRING)
    private IfrsClassification ifrsClassification = IfrsClassification.AMORTIZED_COST;
    
    @Column(name = "aaoifi_standard", length = 50)
    private String aaoifiStandard; // AAOIFI Sharia Standard reference
    
    // Relationships
    @OneToMany(mappedBy = "islamicProduct", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IslamicBankingContract> contracts;
    
    public enum ProductCategory {
        FINANCING, INVESTMENT, DEPOSIT, TRADE_FINANCE, TREASURY, TAKAFUL, SUKUK
    }
    
    public enum IslamicStructure {
        MURABAHA, IJARA, MUSHARAKA, MUDARABA, SALAM, ISTISNA, QARD_HASSAN,
        WAKALA, SUKUK, TAWARRUQ, BITHAMAN_AJIL, DIMINISHING_MUSHARAKA
    }
    
    public enum ProfitCalculationMethod {
        FIXED_RATE, FLOATING_RATE, PROFIT_SHARING, RENTAL_BASED, COST_PLUS_MARKUP
    }
    
    public enum PaymentFrequency {
        MONTHLY, QUARTERLY, SEMI_ANNUALLY, ANNUALLY, BULLET, ON_MATURITY
    }
    
    public enum RiskCategory {
        LOW, MEDIUM, HIGH, VERY_HIGH
    }
    
    public enum IfrsClassification {
        AMORTIZED_COST, FAIR_VALUE_OCI, FAIR_VALUE_PL
    }
}
