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
@Table(name = "loan_products")
public class LoanProduct extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
    
    @Column(name = "product_code", length = 20, unique = true, nullable = false)
    private String productCode;
    
    @Column(name = "product_name", length = 200, nullable = false)
    private String productName;
    
    @Column(name = "product_type")
    @Enumerated(EnumType.STRING)
    private LoanType loanType;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    // Interest Configuration
    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate;
    
    @Column(name = "interest_type")
    @Enumerated(EnumType.STRING)
    private InterestType interestType = InterestType.REDUCING_BALANCE;
    
    @Column(name = "interest_calculation_method")
    @Enumerated(EnumType.STRING)
    private InterestCalculationMethod interestCalculationMethod = InterestCalculationMethod.DAILY;
    
    @Column(name = "compounding_frequency")
    @Enumerated(EnumType.STRING)
    private CompoundingFrequency compoundingFrequency = CompoundingFrequency.MONTHLY;
    
    // Loan Limits
    @Column(name = "minimum_amount", precision = 19, scale = 2)
    private BigDecimal minimumAmount;
    
    @Column(name = "maximum_amount", precision = 19, scale = 2)
    private BigDecimal maximumAmount;
    
    @Column(name = "minimum_term_months")
    private Integer minimumTermMonths;
    
    @Column(name = "maximum_term_months")
    private Integer maximumTermMonths;
    
    // Repayment Configuration
    @Column(name = "repayment_frequency")
    @Enumerated(EnumType.STRING)
    private RepaymentFrequency repaymentFrequency = RepaymentFrequency.MONTHLY;
    
    @Column(name = "grace_period_days")
    private Integer gracePeriodDays = 0;
    
    @Column(name = "moratorium_period_months")
    private Integer moratoriumPeriodMonths = 0;
    
    // Fees and Charges
    @Column(name = "processing_fee_rate", precision = 5, scale = 4)
    private BigDecimal processingFeeRate = BigDecimal.ZERO;
    
    @Column(name = "processing_fee_amount", precision = 19, scale = 2)
    private BigDecimal processingFeeAmount = BigDecimal.ZERO;
    
    @Column(name = "late_payment_penalty_rate", precision = 5, scale = 4)
    private BigDecimal latePaymentPenaltyRate = BigDecimal.ZERO;
    
    @Column(name = "prepayment_penalty_rate", precision = 5, scale = 4)
    private BigDecimal prepaymentPenaltyRate = BigDecimal.ZERO;
    
    // Eligibility Criteria
    @Column(name = "minimum_age")
    private Integer minimumAge = 18;
    
    @Column(name = "maximum_age")
    private Integer maximumAge = 65;
    
    @Column(name = "minimum_income", precision = 19, scale = 2)
    private BigDecimal minimumIncome;
    
    @Column(name = "minimum_credit_score")
    private Integer minimumCreditScore;
    
    @Column(name = "employment_required")
    private Boolean employmentRequired = true;
    
    @Column(name = "collateral_required")
    private Boolean collateralRequired = false;
    
    @Column(name = "guarantor_required")
    private Boolean guarantorRequired = false;
    
    // Risk Assessment
    @Column(name = "risk_category")
    @Enumerated(EnumType.STRING)
    private RiskCategory riskCategory = RiskCategory.MEDIUM;
    
    @Column(name = "provisioning_rate", precision = 5, scale = 4)
    private BigDecimal provisioningRate = BigDecimal.ZERO;
    
    // Islamic Banking Compliance
    @Column(name = "sharia_compliant")
    private Boolean shariaCompliant = false;
    
    @Column(name = "islamic_structure")
    @Enumerated(EnumType.STRING)
    private IslamicStructure islamicStructure;
    
    // Regulatory Compliance
    @Column(name = "basel_compliant")
    private Boolean baselCompliant = true;
    
    @Column(name = "ifrs_compliant")
    private Boolean ifrsCompliant = true;
    
    // Relationships
    @OneToMany(mappedBy = "loanProduct", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Loan> loans;
    
    public enum LoanType {
        PERSONAL, HOME, AUTO, BUSINESS, AGRICULTURE, EDUCATION, 
        CREDIT_CARD, OVERDRAFT, TERM_LOAN, WORKING_CAPITAL
    }
    
    public enum InterestType {
        FLAT_RATE, REDUCING_BALANCE, COMPOUND, SIMPLE
    }
    
    public enum InterestCalculationMethod {
        DAILY, MONTHLY, ANNUALLY, ACTUAL_365, ACTUAL_360
    }
    
    public enum CompoundingFrequency {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, SEMI_ANNUALLY, ANNUALLY
    }
    
    public enum RepaymentFrequency {
        WEEKLY, BI_WEEKLY, MONTHLY, QUARTERLY, SEMI_ANNUALLY, ANNUALLY
    }
    
    public enum RiskCategory {
        LOW, MEDIUM, HIGH, VERY_HIGH
    }
    
    public enum IslamicStructure {
        MURABAHA, IJARA, MUSHARAKA, MUDARABA, ISTISNA, SALAM, TAWARRUQ
    }
}
