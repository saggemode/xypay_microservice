package com.xypay.analytics.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "risk_analytics")
public class RiskAnalytics extends BaseEntity {
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "transaction_id")
    private Long transactionId;
    
    @Column(name = "risk_type", length = 30, nullable = false)
    private String riskType; // CREDIT, FRAUD, OPERATIONAL, MARKET, LIQUIDITY
    
    @Column(name = "risk_score", precision = 10, scale = 4, nullable = false)
    private BigDecimal riskScore;
    
    @Column(name = "risk_level", length = 20)
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    
    @Column(name = "assessment_date", nullable = false)
    private LocalDateTime assessmentDate;
    
    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactors;
    
    @Column(name = "mitigation_actions", columnDefinition = "TEXT")
    private String mitigationActions;
    
    @Column(name = "is_resolved")
    private Boolean isResolved = false;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "model_version", length = 20)
    private String modelVersion = "1.0";
}