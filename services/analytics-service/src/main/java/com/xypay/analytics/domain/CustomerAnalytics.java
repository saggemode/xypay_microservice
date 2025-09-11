package com.xypay.analytics.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customer_analytics")
public class CustomerAnalytics extends BaseEntity {
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "total_transaction_value", precision = 19, scale = 2)
    private BigDecimal totalTransactionValue = BigDecimal.ZERO;
    
    @Column(name = "average_account_balance", precision = 19, scale = 2)
    private BigDecimal averageAccountBalance = BigDecimal.ZERO;
    
    @Column(name = "credit_limit", precision = 19, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;
    
    @Column(name = "credit_used", precision = 19, scale = 2)
    private BigDecimal creditUsed = BigDecimal.ZERO;
    
    @Column(name = "on_time_payments", precision = 10, scale = 0)
    private BigDecimal onTimePayments = BigDecimal.ZERO;
    
    @Column(name = "total_payments", precision = 10, scale = 0)
    private BigDecimal totalPayments = BigDecimal.ZERO;
    
    @Column(name = "account_age_months")
    private Long accountAgeMonths = 0L;
    
    @Column(name = "pattern_consistency", precision = 5, scale = 4)
    private BigDecimal patternConsistency = BigDecimal.ZERO;
    
    @Column(name = "risk_factor_count", precision = 10, scale = 0)
    private BigDecimal riskFactorCount = BigDecimal.ZERO;
    
    @Column(name = "transaction_frequency", precision = 5, scale = 4)
    private BigDecimal transactionFrequency = BigDecimal.ZERO;
    
    @Column(name = "channel_diversity", precision = 5, scale = 4)
    private BigDecimal channelDiversity = BigDecimal.ZERO;
    
    @Column(name = "product_usage", precision = 5, scale = 4)
    private BigDecimal productUsage = BigDecimal.ZERO;
    
    @Column(name = "data_completeness", precision = 5, scale = 4)
    private BigDecimal dataCompleteness = BigDecimal.ZERO;
    
    @Column(name = "data_recency", precision = 5, scale = 4)
    private BigDecimal dataRecency = BigDecimal.ZERO;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
