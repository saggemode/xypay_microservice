package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "anomaly_detection", indexes = {
    @Index(name = "idx_anomaly_user", columnList = "user_id, is_anomaly"),
    @Index(name = "idx_anomaly_investigation", columnList = "investigation_status"),
    @Index(name = "idx_anomaly_detected", columnList = "detected_at")
})
public class AnomalyDetection extends BaseEntity {
    
    public enum AnomalyType {
        POINT_ANOMALY("point_anomaly", "Point Anomaly"),
        CONTEXTUAL_ANOMALY("contextual_anomaly", "Contextual Anomaly"),
        COLLECTIVE_ANOMALY("collective_anomaly", "Collective Anomaly");
        
        private final String code;
        private final String displayName;
        
        AnomalyType(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum InvestigationStatus {
        PENDING("pending", "Pending"),
        IN_PROGRESS("in_progress", "In Progress"),
        RESOLVED("resolved", "Resolved");
        
        private final String code;
        private final String displayName;
        
        InvestigationStatus(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ResolutionType {
        TRUE_POSITIVE("true_positive", "True Positive"),
        FALSE_POSITIVE("false_positive", "False Positive"),
        INCONCLUSIVE("inconclusive", "Inconclusive");
        
        private final String code;
        private final String displayName;
        
        ResolutionType(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private BankTransfer transaction;
    
    // Anomaly scores
    @Min(0)
    @Max(1)
    @Column(name = "isolation_forest_score", nullable = false)
    private Double isolationForestScore;
    
    @Min(0)
    @Max(1)
    @Column(name = "local_outlier_score", nullable = false)
    private Double localOutlierScore;
    
    @Min(0)
    @Max(1)
    @Column(name = "autoencoder_score", nullable = false)
    private Double autoencoderScore;
    
    // Contextual features
    @Column(name = "temporal_features", columnDefinition = "JSON", nullable = false)
    private String temporalFeatures;
    
    @Column(name = "behavioral_features", columnDefinition = "JSON", nullable = false)
    private String behavioralFeatures;
    
    @Column(name = "transaction_features", columnDefinition = "JSON", nullable = false)
    private String transactionFeatures;
    
    // Anomaly classification
    @Enumerated(EnumType.STRING)
    @Column(name = "anomaly_type", length = 50, nullable = false)
    private AnomalyType anomalyType;
    
    @Min(0)
    @Max(1)
    @Column(name = "confidence", nullable = false)
    private Double confidence;
    
    // Decision thresholds
    @Column(name = "threshold_config", columnDefinition = "JSON", nullable = false)
    private String thresholdConfig;
    
    @Column(name = "is_anomaly", nullable = false)
    private Boolean isAnomaly = false;
    
    // Investigation status
    @Column(name = "requires_investigation", nullable = false)
    private Boolean requiresInvestigation = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "investigation_status", length = 20, nullable = false)
    private InvestigationStatus investigationStatus = InvestigationStatus.PENDING;
    
    @Column(name = "investigation_notes", columnDefinition = "TEXT")
    private String investigationNotes;
    
    // Response actions
    @Column(name = "actions_taken", columnDefinition = "JSON", nullable = false)
    private String actionsTaken = "[]";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "resolution", length = 50)
    private ResolutionType resolution;
    
    // Metadata
    @Column(name = "detected_at", nullable = false)
    private java.time.LocalDateTime detectedAt;
    
    // Constructors
    public AnomalyDetection() {
        this.detectedAt = java.time.LocalDateTime.now();
    }
    
    public AnomalyDetection(User user, BankTransfer transaction, Double isolationForestScore, 
                           Double localOutlierScore, Double autoencoderScore, String temporalFeatures,
                           String behavioralFeatures, String transactionFeatures, AnomalyType anomalyType,
                           Double confidence, String thresholdConfig) {
        this();
        this.user = user;
        this.transaction = transaction;
        this.isolationForestScore = isolationForestScore;
        this.localOutlierScore = localOutlierScore;
        this.autoencoderScore = autoencoderScore;
        this.temporalFeatures = temporalFeatures;
        this.behavioralFeatures = behavioralFeatures;
        this.transactionFeatures = transactionFeatures;
        this.anomalyType = anomalyType;
        this.confidence = confidence;
        this.thresholdConfig = thresholdConfig;
    }
    
    // Business methods
    public Double calculateCompositeScore() {
        double weights = 0.4; // isolation_forest
        double localWeight = 0.3; // local_outlier
        double autoWeight = 0.3; // autoencoder
        
        double compositeScore = (weights * isolationForestScore) +
                               (localWeight * localOutlierScore) +
                               (autoWeight * autoencoderScore);
        
        return Math.min(compositeScore, 1.0);
    }
    
    public void updateInvestigationStatus(InvestigationStatus status, String notes) {
        this.investigationStatus = status;
        if (notes != null) {
            this.investigationNotes = notes;
        }
    }
    
    public void markResolution(ResolutionType resolutionType, String actions) {
        this.resolution = resolutionType;
        if (actions != null) {
            this.actionsTaken = actions;
        }
    }
}
