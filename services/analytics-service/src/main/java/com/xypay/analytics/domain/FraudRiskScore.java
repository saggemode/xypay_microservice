package com.xypay.analytics.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "fraud_risk_scores")
public class FraudRiskScore extends BaseEntity {
    
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;
    
    @Column(name = "risk_score", precision = 10, scale = 4, nullable = false)
    private BigDecimal riskScore;
    
    @Column(name = "assessment_date", nullable = false)
    private LocalDateTime assessmentDate;
    
    @Column(name = "risk_level", length = 20)
    private String riskLevel;
    
    @ElementCollection
    @CollectionTable(name = "fraud_risk_factors", joinColumns = @JoinColumn(name = "fraud_risk_score_id"))
    @MapKeyColumn(name = "factor_name")
    @Column(name = "factor_value")
    private Map<String, BigDecimal> riskFactors;
    
    @ElementCollection
    @CollectionTable(name = "fraud_recommendations", joinColumns = @JoinColumn(name = "fraud_risk_score_id"))
    @Column(name = "recommendation")
    private List<String> recommendations;
    
    @Column(name = "is_processed")
    private Boolean isProcessed = false;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "model_version", length = 20)
    private String modelVersion = "1.0";
}