package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "behavioral_analysis", indexes = {
    @Index(name = "idx_behavioral_user_trust", columnList = "user_id, trust_score"),
    @Index(name = "idx_behavioral_analyzed", columnList = "last_analyzed")
})
public class BehavioralAnalysis extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Temporal patterns
    @Column(name = "active_hours", columnDefinition = "JSON", nullable = false)
    private String activeHours;
    
    @Column(name = "active_days", columnDefinition = "JSON", nullable = false)
    private String activeDays;
    
    @Column(name = "session_patterns", columnDefinition = "JSON", nullable = false)
    private String sessionPatterns;
    
    // Transaction patterns
    @Column(name = "amount_patterns", columnDefinition = "JSON", nullable = false)
    private String amountPatterns;
    
    @Column(name = "frequency_patterns", columnDefinition = "JSON", nullable = false)
    private String frequencyPatterns;
    
    @Column(name = "recipient_patterns", columnDefinition = "JSON", nullable = false)
    private String recipientPatterns;
    
    // Location patterns
    @Column(name = "location_clusters", columnDefinition = "JSON", nullable = false)
    private String locationClusters;
    
    @Column(name = "travel_patterns", columnDefinition = "JSON", nullable = false)
    private String travelPatterns;
    
    @Column(name = "typical_radius", nullable = false)
    private Double typicalRadius;
    
    // Device patterns
    @Column(name = "device_fingerprints", columnDefinition = "JSON", nullable = false)
    private String deviceFingerprints;
    
    @Column(name = "device_usage_patterns", columnDefinition = "JSON", nullable = false)
    private String deviceUsagePatterns;
    
    // Behavioral scores
    @Min(0)
    @Max(1)
    @Column(name = "regularity_score", nullable = false)
    private Double regularityScore;
    
    @Min(0)
    @Max(1)
    @Column(name = "risk_appetite", nullable = false)
    private Double riskAppetite;
    
    @Min(0)
    @Max(1)
    @Column(name = "trust_score", nullable = false)
    private Double trustScore;
    
    // Analysis metadata
    @Column(name = "last_analyzed", nullable = false)
    private java.time.LocalDateTime lastAnalyzed;
    
    @Column(name = "data_points", nullable = false)
    private Integer dataPoints;
    
    @Min(0)
    @Max(1)
    @Column(name = "confidence_level", nullable = false)
    private Double confidenceLevel;
    
    // Constructors
    public BehavioralAnalysis() {
        this.lastAnalyzed = java.time.LocalDateTime.now();
        this.dataPoints = 0;
    }
    
    public BehavioralAnalysis(User user, String activeHours, String activeDays, String sessionPatterns,
                             String amountPatterns, String frequencyPatterns, String recipientPatterns,
                             String locationClusters, String travelPatterns, Double typicalRadius,
                             String deviceFingerprints, String deviceUsagePatterns, Double regularityScore,
                             Double riskAppetite, Double trustScore, Integer dataPoints, Double confidenceLevel) {
        this();
        this.user = user;
        this.activeHours = activeHours;
        this.activeDays = activeDays;
        this.sessionPatterns = sessionPatterns;
        this.amountPatterns = amountPatterns;
        this.frequencyPatterns = frequencyPatterns;
        this.recipientPatterns = recipientPatterns;
        this.locationClusters = locationClusters;
        this.travelPatterns = travelPatterns;
        this.typicalRadius = typicalRadius;
        this.deviceFingerprints = deviceFingerprints;
        this.deviceUsagePatterns = deviceUsagePatterns;
        this.regularityScore = regularityScore;
        this.riskAppetite = riskAppetite;
        this.trustScore = trustScore;
        this.dataPoints = dataPoints;
        this.confidenceLevel = confidenceLevel;
    }
    
    // Business methods
    public void updatePatterns(String newActivityData) {
        // Update temporal patterns
        this.updateTemporalPatterns(newActivityData);
        
        // Update location patterns
        this.updateLocationPatterns(newActivityData);
        
        // Update device patterns
        this.updateDevicePatterns(newActivityData);
        
        // Recalculate scores
        this.recalculateScores();
        
        this.dataPoints++;
        this.lastAnalyzed = java.time.LocalDateTime.now();
    }
    
    private void updateTemporalPatterns(String activityData) {
        // Implementation would parse activityData and update temporal patterns
        // This is a placeholder for the actual implementation
    }
    
    private void updateLocationPatterns(String activityData) {
        // Implementation would parse activityData and update location patterns
        // This is a placeholder for the actual implementation
    }
    
    private void updateDevicePatterns(String activityData) {
        // Implementation would parse activityData and update device patterns
        // This is a placeholder for the actual implementation
    }
    
    private void recalculateScores() {
        // Implementation would recalculate behavioral scores based on updated patterns
        // This is a placeholder for the actual implementation
    }
}