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
@Table(name = "loans")
public class Loan extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Wallet account;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;
    
    @Column(name = "loan_number", length = 50, unique = true, nullable = false)
    private String loanNumber;
    
    @Column(name = "principal_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal principalAmount;
    
    @Column(name = "disbursed_amount", precision = 19, scale = 2)
    private BigDecimal disbursedAmount = BigDecimal.ZERO;
    
    @Column(name = "outstanding_principal", precision = 19, scale = 2)
    private BigDecimal outstandingPrincipal = BigDecimal.ZERO;
    
    @Column(name = "outstanding_interest", precision = 19, scale = 2)
    private BigDecimal outstandingInterest = BigDecimal.ZERO;
    
    @Column(name = "total_outstanding", precision = 19, scale = 2)
    private BigDecimal totalOutstanding = BigDecimal.ZERO;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate;
    
    @Column(name = "loan_term_months")
    private Integer loanTermMonths;
    
    @Column(name = "remaining_term_months")
    private Integer remainingTermMonths;
    
    @Column(name = "application_date")
    private LocalDateTime applicationDate;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column(name = "disbursement_date")
    private LocalDateTime disbursementDate;
    
    @Column(name = "maturity_date")
    private LocalDateTime maturityDate;
    
    @Column(name = "first_payment_date")
    private LocalDateTime firstPaymentDate;
    
    @Column(name = "next_payment_date")
    private LocalDateTime nextPaymentDate;
    
    @Column(name = "last_payment_date")
    private LocalDateTime lastPaymentDate;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.APPLIED;
    
    @Column(name = "repayment_frequency")
    @Enumerated(EnumType.STRING)
    private RepaymentFrequency repaymentFrequency;
    
    @Column(name = "monthly_payment_amount", precision = 19, scale = 2)
    private BigDecimal monthlyPaymentAmount;
    
    // Risk and Provisioning
    @Column(name = "risk_rating")
    @Enumerated(EnumType.STRING)
    private RiskRating riskRating = RiskRating.STANDARD;
    
    @Column(name = "days_past_due")
    private Integer daysPastDue = 0;
    
    @Column(name = "provision_amount", precision = 19, scale = 2)
    private BigDecimal provisionAmount = BigDecimal.ZERO;
    
    @Column(name = "provision_rate", precision = 5, scale = 4)
    private BigDecimal provisionRate = BigDecimal.ZERO;
    
    @Column(name = "impairment_stage")
    @Enumerated(EnumType.STRING)
    private ImpairmentStage impairmentStage = ImpairmentStage.STAGE_1;
    
    // Islamic Banking
    @Column(name = "sharia_compliant")
    private Boolean shariaCompliant = false;
    
    @Column(name = "islamic_structure")
    @Enumerated(EnumType.STRING)
    private IslamicStructure islamicStructure;
    
    @Column(name = "profit_rate", precision = 5, scale = 4)
    private BigDecimal profitRate; // For Islamic banking
    
    // Collateral and Guarantees
    @Column(name = "collateral_value", precision = 19, scale = 2)
    private BigDecimal collateralValue = BigDecimal.ZERO;
    
    @Column(name = "loan_to_value_ratio", precision = 5, scale = 2)
    private BigDecimal loanToValueRatio = BigDecimal.ZERO;
    
    @Column(name = "guarantor_required")
    private Boolean guarantorRequired = false;
    
    // Workflow and Approval
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approval_workflow_id")
    private Long approvalWorkflowId;
    
    // Regulatory Compliance
    @Column(name = "basel_classification")
    @Enumerated(EnumType.STRING)
    private BaselClassification baselClassification = BaselClassification.STANDARD;
    
    @Column(name = "ifrs_classification")
    @Enumerated(EnumType.STRING)
    private IfrsClassification ifrsClassification = IfrsClassification.STAGE_1;
    
    // Relationships
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoanRepayment> repayments;
    
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoanAmortization> amortizationSchedule;
    
    public enum LoanStatus {
        APPLIED, UNDER_REVIEW, APPROVED, REJECTED, DISBURSED, ACTIVE, 
        OVERDUE, RESTRUCTURED, WRITTEN_OFF, CLOSED, CANCELLED
    }
    
    public enum RepaymentFrequency {
        WEEKLY, BI_WEEKLY, MONTHLY, QUARTERLY, SEMI_ANNUALLY, ANNUALLY
    }
    
    public enum RiskRating {
        EXCELLENT, GOOD, STANDARD, SUBSTANDARD, DOUBTFUL, LOSS
    }
    
    public enum ImpairmentStage {
        STAGE_1, STAGE_2, STAGE_3 // IFRS 9 stages
    }
    
    public enum IslamicStructure {
        MURABAHA, IJARA, MUSHARAKA, MUDARABA, ISTISNA, SALAM, TAWARRUQ
    }
    
    public enum BaselClassification {
        STANDARD, SPECIAL_MENTION, SUBSTANDARD, DOUBTFUL, LOSS
    }
    
    public enum IfrsClassification {
        STAGE_1, STAGE_2, STAGE_3
    }
}