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
@Table(name = "islamic_banking_contracts")
public class IslamicBankingContract extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "islamic_product_id", nullable = false)
    private IslamicBankingProduct islamicProduct;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;
    
    @Column(name = "contract_number", length = 50, unique = true, nullable = false)
    private String contractNumber;
    
    @Column(name = "contract_status")
    @Enumerated(EnumType.STRING)
    private ContractStatus contractStatus = ContractStatus.DRAFT;
    
    @Column(name = "contract_date")
    private LocalDateTime contractDate;
    
    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;
    
    @Column(name = "maturity_date")
    private LocalDateTime maturityDate;
    
    // Financial Terms
    @Column(name = "principal_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal principalAmount;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    @Column(name = "profit_rate", precision = 5, scale = 4)
    private BigDecimal profitRate;
    
    @Column(name = "customer_profit_share", precision = 5, scale = 2)
    private BigDecimal customerProfitShare;
    
    @Column(name = "bank_profit_share", precision = 5, scale = 2)
    private BigDecimal bankProfitShare;
    
    @Column(name = "total_profit_expected", precision = 19, scale = 2)
    private BigDecimal totalProfitExpected = BigDecimal.ZERO;
    
    @Column(name = "total_amount_payable", precision = 19, scale = 2)
    private BigDecimal totalAmountPayable;
    
    @Column(name = "monthly_installment", precision = 19, scale = 2)
    private BigDecimal monthlyInstallment;
    
    // Outstanding Balances
    @Column(name = "outstanding_principal", precision = 19, scale = 2)
    private BigDecimal outstandingPrincipal;
    
    @Column(name = "outstanding_profit", precision = 19, scale = 2)
    private BigDecimal outstandingProfit = BigDecimal.ZERO;
    
    @Column(name = "total_paid", precision = 19, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;
    
    @Column(name = "profit_paid", precision = 19, scale = 2)
    private BigDecimal profitPaid = BigDecimal.ZERO;
    
    @Column(name = "principal_paid", precision = 19, scale = 2)
    private BigDecimal principalPaid = BigDecimal.ZERO;
    
    // Sharia Compliance
    @Column(name = "sharia_board_approved")
    private Boolean shariaBoardApproved = false;
    
    @Column(name = "sharia_approval_date")
    private LocalDateTime shariaApprovalDate;
    
    @Column(name = "sharia_approval_reference", length = 100)
    private String shariaApprovalReference;
    
    @Column(name = "sharia_advisor", length = 200)
    private String shariaAdvisor;
    
    @Column(name = "aaoifi_compliance")
    private Boolean aaoifiCompliance = true;
    
    // Asset/Commodity Details (for asset-backed structures)
    @Column(name = "underlying_asset", length = 500)
    private String underlyingAsset;
    
    @Column(name = "asset_value", precision = 19, scale = 2)
    private BigDecimal assetValue;
    
    @Column(name = "asset_ownership_transferred")
    private Boolean assetOwnershipTransferred = false;
    
    @Column(name = "asset_delivery_date")
    private LocalDateTime assetDeliveryDate;
    
    @Column(name = "asset_location", length = 200)
    private String assetLocation;
    
    // Rental/Lease Details (for Ijara)
    @Column(name = "rental_amount", precision = 19, scale = 2)
    private BigDecimal rentalAmount;
    
    @Column(name = "rental_frequency")
    @Enumerated(EnumType.STRING)
    private RentalFrequency rentalFrequency;
    
    @Column(name = "maintenance_responsibility")
    @Enumerated(EnumType.STRING)
    private MaintenanceResponsibility maintenanceResponsibility;
    
    @Column(name = "insurance_responsibility")
    @Enumerated(EnumType.STRING)
    private InsuranceResponsibility insuranceResponsibility;
    
    // Partnership Details (for Musharaka/Mudaraba)
    @Column(name = "bank_capital_contribution", precision = 19, scale = 2)
    private BigDecimal bankCapitalContribution;
    
    @Column(name = "customer_capital_contribution", precision = 19, scale = 2)
    private BigDecimal customerCapitalContribution;
    
    @Column(name = "management_responsibility")
    @Enumerated(EnumType.STRING)
    private ManagementResponsibility managementResponsibility;
    
    @Column(name = "business_activity", length = 500)
    private String businessActivity;
    
    @Column(name = "actual_profit_earned", precision = 19, scale = 2)
    private BigDecimal actualProfitEarned = BigDecimal.ZERO;
    
    @Column(name = "loss_incurred", precision = 19, scale = 2)
    private BigDecimal lossIncurred = BigDecimal.ZERO;
    
    // Risk Management
    @Column(name = "risk_rating")
    @Enumerated(EnumType.STRING)
    private RiskRating riskRating = RiskRating.STANDARD;
    
    @Column(name = "collateral_required")
    private Boolean collateralRequired = false;
    
    @Column(name = "collateral_value", precision = 19, scale = 2)
    private BigDecimal collateralValue;
    
    @Column(name = "guarantee_required")
    private Boolean guaranteeRequired = false;
    
    @Column(name = "guarantor_details", length = 500)
    private String guarantorDetails;
    
    // Payment and Settlement
    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod = PaymentMethod.BANK_TRANSFER;
    
    @Column(name = "payment_account", length = 50)
    private String paymentAccount;
    
    @Column(name = "next_payment_date")
    private LocalDateTime nextPaymentDate;
    
    @Column(name = "last_payment_date")
    private LocalDateTime lastPaymentDate;
    
    @Column(name = "days_past_due")
    private Integer daysPastDue = 0;
    
    // Charity and Penalties
    @Column(name = "charity_amount", precision = 19, scale = 2)
    private BigDecimal charityAmount = BigDecimal.ZERO; // Late payment penalties go to charity
    
    @Column(name = "charity_paid", precision = 19, scale = 2)
    private BigDecimal charityPaid = BigDecimal.ZERO;
    
    // Workflow and Approval
    @Column(name = "approval_workflow_id")
    private Long approvalWorkflowId;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column(name = "disbursement_date")
    private LocalDateTime disbursementDate;
    
    // Accounting
    @Column(name = "ifrs_stage")
    @Enumerated(EnumType.STRING)
    private IfrsStage ifrsStage = IfrsStage.STAGE_1;
    
    @Column(name = "provision_amount", precision = 19, scale = 2)
    private BigDecimal provisionAmount = BigDecimal.ZERO;
    
    @Column(name = "regulatory_capital", precision = 19, scale = 2)
    private BigDecimal regulatoryCapital;
    
    // Relationships
    @OneToMany(mappedBy = "islamicContract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IslamicPayment> payments;
    
    @OneToMany(mappedBy = "islamicContract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IslamicProfitDistribution> profitDistributions;
    
    public enum ContractStatus {
        DRAFT, PENDING_APPROVAL, APPROVED, ACTIVE, MATURED, CANCELLED, DEFAULTED
    }
    
    public enum RentalFrequency {
        MONTHLY, QUARTERLY, SEMI_ANNUALLY, ANNUALLY
    }
    
    public enum MaintenanceResponsibility {
        BANK, CUSTOMER, SHARED
    }
    
    public enum InsuranceResponsibility {
        BANK, CUSTOMER, SHARED
    }
    
    public enum ManagementResponsibility {
        BANK, CUSTOMER, SHARED
    }
    
    public enum RiskRating {
        EXCELLENT, GOOD, STANDARD, SUBSTANDARD, DOUBTFUL, LOSS
    }
    
    public enum PaymentMethod {
        BANK_TRANSFER, DIRECT_DEBIT, CASH, CHEQUE, ONLINE
    }
    
    public enum IfrsStage {
        STAGE_1, STAGE_2, STAGE_3
    }
}
