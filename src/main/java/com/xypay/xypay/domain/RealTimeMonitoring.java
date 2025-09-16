package com.xypay.xypay.domain;

import com.xypay.xypay.enums.SecurityLevel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "realtime_monitoring", indexes = {
    @Index(name = "idx_monitoring_user_created", columnList = "user_id, monitored_at"),
    @Index(name = "idx_monitoring_alert_level", columnList = "alert_level"),
    @Index(name = "idx_monitoring_review", columnList = "requires_review")
})
public class RealTimeMonitoring extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private BankTransfer transaction;
    
    // Real-time metrics
    @Min(0)
    @Max(1)
    @Column(name = "velocity_score", nullable = false)
    private Double velocityScore;
    
    @Min(0)
    @Max(1)
    @Column(name = "amount_anomaly_score", nullable = false)
    private Double amountAnomalyScore;
    
    @Min(0)
    @Max(1)
    @Column(name = "pattern_deviation_score", nullable = false)
    private Double patternDeviationScore;
    
    @Min(0)
    @Max(1)
    @Column(name = "location_risk_score", nullable = false)
    private Double locationRiskScore;
    
    @Min(0)
    @Max(1)
    @Column(name = "device_risk_score", nullable = false)
    private Double deviceRiskScore;
    
    // Behavioral patterns
    @Column(name = "user_patterns", columnDefinition = "JSON", nullable = false)
    private String userPatterns;
    
    @Column(name = "transaction_patterns", columnDefinition = "JSON", nullable = false)
    private String transactionPatterns;
    
    @Column(name = "device_patterns", columnDefinition = "JSON", nullable = false)
    private String devicePatterns;
    
    // ML model outputs
    @Column(name = "model_predictions", columnDefinition = "JSON", nullable = false)
    private String modelPredictions;
    
    @Column(name = "confidence_scores", columnDefinition = "JSON", nullable = false)
    private String confidenceScores;
    
    @Column(name = "feature_contributions", columnDefinition = "JSON", nullable = false)
    private String featureContributions;
    
    // Alert flags
    @Column(name = "requires_review", nullable = false)
    private Boolean requiresReview = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_level", length = 20, nullable = false)
    private SecurityLevel alertLevel = SecurityLevel.LOW;
    
    @Column(name = "alerts_generated", columnDefinition = "JSON", nullable = false)
    private String alertsGenerated = "[]";
    
    // Monitoring metadata
    @Column(name = "monitored_at", nullable = false)
    private java.time.LocalDateTime monitoredAt;
    
    @Column(name = "processing_time", nullable = false)
    private Double processingTime;
    
    // Constructors
    public RealTimeMonitoring() {
        this.monitoredAt = java.time.LocalDateTime.now();
    }
    
    public RealTimeMonitoring(User user, BankTransfer transaction, Double velocityScore, 
                             Double amountAnomalyScore, Double patternDeviationScore, 
                             Double locationRiskScore, Double deviceRiskScore, 
                             String userPatterns, String transactionPatterns, String devicePatterns,
                             String modelPredictions, String confidenceScores, String featureContributions,
                             Double processingTime) {
        this();
        this.user = user;
        this.transaction = transaction;
        this.velocityScore = velocityScore;
        this.amountAnomalyScore = amountAnomalyScore;
        this.patternDeviationScore = patternDeviationScore;
        this.locationRiskScore = locationRiskScore;
        this.deviceRiskScore = deviceRiskScore;
        this.userPatterns = userPatterns;
        this.transactionPatterns = transactionPatterns;
        this.devicePatterns = devicePatterns;
        this.modelPredictions = modelPredictions;
        this.confidenceScores = confidenceScores;
        this.featureContributions = featureContributions;
        this.processingTime = processingTime;
    }
    
    // Business methods
    public Double calculateCompositeRisk() {
        double weights = 0.2; // velocity
        double amountWeight = 0.25; // amount
        double patternWeight = 0.2; // pattern
        double locationWeight = 0.15; // location
        double deviceWeight = 0.2; // device
        
        double compositeScore = (weights * velocityScore) +
                               (amountWeight * amountAnomalyScore) +
                               (patternWeight * patternDeviationScore) +
                               (locationWeight * locationRiskScore) +
                               (deviceWeight * deviceRiskScore);
        
        return Math.min(compositeScore, 1.0);
    }
}
