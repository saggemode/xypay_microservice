package com.xypay.analytics.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transaction_analytics")
public class TransactionAnalytics extends BaseEntity {
    
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;
    
    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", length = 3)
    private String currency;
    
    @Column(name = "transaction_type", length = 20)
    private String transactionType;
    
    @Column(name = "channel", length = 20)
    private String channel;
    
    @Column(name = "pattern_anomaly_score", precision = 5, scale = 4)
    private BigDecimal patternAnomalyScore = BigDecimal.ZERO;
    
    @Column(name = "transaction_velocity", precision = 5, scale = 2)
    private BigDecimal transactionVelocity = BigDecimal.ZERO;
    
    @Column(name = "location_risk_score", precision = 5, scale = 4)
    private BigDecimal locationRiskScore = BigDecimal.ZERO;
    
    @Column(name = "time_risk_score", precision = 5, scale = 4)
    private BigDecimal timeRiskScore = BigDecimal.ZERO;
    
    @Column(name = "amount_risk_score", precision = 5, scale = 4)
    private BigDecimal amountRiskScore = BigDecimal.ZERO;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
