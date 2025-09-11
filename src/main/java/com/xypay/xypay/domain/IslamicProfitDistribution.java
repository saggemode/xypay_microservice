package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "islamic_profit_distributions")
public class IslamicProfitDistribution extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "islamic_contract_id", nullable = false)
    private IslamicBankingContract islamicContract;
    
    @Column(name = "distribution_date")
    private LocalDateTime distributionDate;
    
    @Column(name = "period_start_date")
    private LocalDateTime periodStartDate;
    
    @Column(name = "period_end_date")
    private LocalDateTime periodEndDate;
    
    @Column(name = "gross_profit", precision = 19, scale = 2)
    private BigDecimal grossProfit;
    
    @Column(name = "allowable_expenses", precision = 19, scale = 2)
    private BigDecimal allowableExpenses = BigDecimal.ZERO;
    
    @Column(name = "net_profit", precision = 19, scale = 2)
    private BigDecimal netProfit;
    
    @Column(name = "customer_share_percentage", precision = 5, scale = 2)
    private BigDecimal customerSharePercentage;
    
    @Column(name = "bank_share_percentage", precision = 5, scale = 2)
    private BigDecimal bankSharePercentage;
    
    @Column(name = "customer_profit_amount", precision = 19, scale = 2)
    private BigDecimal customerProfitAmount;
    
    @Column(name = "bank_profit_amount", precision = 19, scale = 2)
    private BigDecimal bankProfitAmount;
    
    @Column(name = "distribution_status")
    @Enumerated(EnumType.STRING)
    private DistributionStatus distributionStatus = DistributionStatus.CALCULATED;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "payment_reference", length = 100)
    private String paymentReference;
    
    @Column(name = "sharia_compliance_verified")
    private Boolean shariaComplianceVerified = false;
    
    @Column(name = "verified_by", length = 200)
    private String verifiedBy;
    
    @Column(name = "verification_date")
    private LocalDateTime verificationDate;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    public enum DistributionStatus {
        CALCULATED, APPROVED, PAID, CANCELLED
    }
}
