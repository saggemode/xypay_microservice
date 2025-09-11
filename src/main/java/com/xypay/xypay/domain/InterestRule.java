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
@Table(name = "interest_rules")
public class InterestRule extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
    
    @Column(name = "rule_code", length = 20, unique = true, nullable = false)
    private String ruleCode;
    
    @Column(name = "rule_name", length = 200, nullable = false)
    private String ruleName;
    
    @Column(name = "rule_description", length = 500)
    private String ruleDescription;
    
    @Column(name = "rule_type")
    @Enumerated(EnumType.STRING)
    private RuleType ruleType;
    
    @Column(name = "product_category")
    @Enumerated(EnumType.STRING)
    private ProductCategory productCategory;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    // Interest Rate Configuration
    @Column(name = "base_rate", precision = 5, scale = 4)
    private BigDecimal baseRate;
    
    @Column(name = "spread", precision = 5, scale = 4)
    private BigDecimal spread = BigDecimal.ZERO;
    
    @Column(name = "minimum_rate", precision = 5, scale = 4)
    private BigDecimal minimumRate;
    
    @Column(name = "maximum_rate", precision = 5, scale = 4)
    private BigDecimal maximumRate;
    
    @Column(name = "interest_type")
    @Enumerated(EnumType.STRING)
    private InterestType interestType = InterestType.REDUCING_BALANCE;
    
    @Column(name = "calculation_method")
    @Enumerated(EnumType.STRING)
    private CalculationMethod calculationMethod = CalculationMethod.DAILY;
    
    @Column(name = "compounding_frequency")
    @Enumerated(EnumType.STRING)
    private CompoundingFrequency compoundingFrequency = CompoundingFrequency.MONTHLY;
    
    @Column(name = "day_count_basis")
    @Enumerated(EnumType.STRING)
    private DayCountBasis dayCountBasis = DayCountBasis.ACTUAL_365;
    
    // Tiered Interest Configuration
    @Column(name = "is_tiered")
    private Boolean isTiered = false;
    
    @OneToMany(mappedBy = "interestRule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InterestTier> interestTiers;
    
    // Conditions and Criteria
    @Column(name = "minimum_balance", precision = 19, scale = 2)
    private BigDecimal minimumBalance;
    
    @Column(name = "maximum_balance", precision = 19, scale = 2)
    private BigDecimal maximumBalance;
    
    @Column(name = "minimum_term_days")
    private Integer minimumTermDays;
    
    @Column(name = "maximum_term_days")
    private Integer maximumTermDays;
    
    @Column(name = "customer_category", length = 50)
    private String customerCategory; // RETAIL, CORPORATE, SME, VIP
    
    @Column(name = "relationship_tier", length = 50)
    private String relationshipTier; // BRONZE, SILVER, GOLD, PLATINUM
    
    // Islamic Banking Support
    @Column(name = "sharia_compliant")
    private Boolean shariaCompliant = false;
    
    @Column(name = "profit_sharing_ratio", precision = 5, scale = 4)
    private BigDecimal profitSharingRatio;
    
    @Column(name = "islamic_structure")
    @Enumerated(EnumType.STRING)
    private IslamicStructure islamicStructure;
    
    // Regulatory and Risk
    @Column(name = "risk_category")
    @Enumerated(EnumType.STRING)
    private RiskCategory riskCategory = RiskCategory.MEDIUM;
    
    @Column(name = "basel_compliant")
    private Boolean baselCompliant = true;
    
    @Column(name = "ifrs_compliant")
    private Boolean ifrsCompliant = true;
    
    // Approval and Authorization
    @Column(name = "created_by", length = 100, nullable = false)
    private String createdBy;
    
    @Column(name = "approved_by", length = 100)
    private String approvedBy;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column(name = "approval_status")
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    
    // Formula and Custom Logic
    @Column(name = "custom_formula", length = 1000)
    private String customFormula;
    
    @Column(name = "calculation_script", columnDefinition = "TEXT")
    private String calculationScript;
    
    public enum RuleType {
        DEPOSIT_INTEREST, LOAN_INTEREST, OVERDRAFT_INTEREST, 
        SAVINGS_INTEREST, FIXED_DEPOSIT_INTEREST, PENALTY_INTEREST,
        ISLAMIC_PROFIT_SHARING, TREASURY_INTEREST
    }
    
    public enum ProductCategory {
        SAVINGS, CURRENT, FIXED_DEPOSIT, LOANS, OVERDRAFT, 
        CREDIT_CARD, TREASURY, ISLAMIC_BANKING
    }
    
    public enum InterestType {
        FLAT_RATE, REDUCING_BALANCE, COMPOUND, SIMPLE, 
        VARIABLE, FIXED, FLOATING
    }
    
    public enum CalculationMethod {
        DAILY, MONTHLY, QUARTERLY, SEMI_ANNUALLY, ANNUALLY,
        ACTUAL_365, ACTUAL_360, THIRTY_360
    }
    
    public enum CompoundingFrequency {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, 
        SEMI_ANNUALLY, ANNUALLY, CONTINUOUS
    }
    
    public enum DayCountBasis {
        ACTUAL_365, ACTUAL_360, THIRTY_360, 
        ACTUAL_ACTUAL, THIRTY_365
    }
    
    public enum IslamicStructure {
        MURABAHA, IJARA, MUSHARAKA, MUDARABA, 
        ISTISNA, SALAM, TAWARRUQ, WAKALA
    }
    
    public enum RiskCategory {
        LOW, MEDIUM, HIGH, VERY_HIGH
    }
    
    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED, EXPIRED
    }
    
    // Helper methods
    public BigDecimal getEffectiveRate() {
        return baseRate.add(spread);
    }
    
    public boolean isValidForAmount(BigDecimal amount) {
        if (minimumBalance != null && amount.compareTo(minimumBalance) < 0) {
            return false;
        }
        if (maximumBalance != null && amount.compareTo(maximumBalance) > 0) {
            return false;
        }
        return true;
    }
    
    public boolean isValidForTerm(Integer termDays) {
        if (minimumTermDays != null && termDays < minimumTermDays) {
            return false;
        }
        if (maximumTermDays != null && termDays > maximumTermDays) {
            return false;
        }
        return true;
    }
    
    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        if (!isActive) return false;
        if (effectiveDate.isAfter(now)) return false;
        if (expiryDate != null && expiryDate.isBefore(now)) return false;
        return approvalStatus == ApprovalStatus.APPROVED;
    }
    
    public BigDecimal calculateInterest(BigDecimal principal, Integer days) {
        if (!isCurrentlyActive()) {
            throw new IllegalStateException("Interest rule is not active");
        }
        
        BigDecimal effectiveRate = getEffectiveRate();
        BigDecimal dailyRate = effectiveRate.divide(new BigDecimal("365"), 8, java.math.RoundingMode.HALF_UP);
        
        switch (calculationMethod) {
            case DAILY:
                return principal.multiply(dailyRate).multiply(new BigDecimal(days));
            case MONTHLY:
                BigDecimal monthlyRate = effectiveRate.divide(new BigDecimal("12"), 8, java.math.RoundingMode.HALF_UP);
                return principal.multiply(monthlyRate).multiply(new BigDecimal(days).divide(new BigDecimal("30"), 8, java.math.RoundingMode.HALF_UP));
            case ANNUALLY:
                return principal.multiply(effectiveRate).multiply(new BigDecimal(days).divide(new BigDecimal("365"), 8, java.math.RoundingMode.HALF_UP));
            default:
                return principal.multiply(dailyRate).multiply(new BigDecimal(days));
        }
    }
}
