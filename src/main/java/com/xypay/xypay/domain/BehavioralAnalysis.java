package com.xypay.xypay.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "behavioral_analysis", indexes = {
    @Index(name = "idx_behavioral_user_trust", columnList = "user_id, trust_score"),
    @Index(name = "idx_behavioral_last_analyzed", columnList = "last_analyzed")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BehavioralAnalysis extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Temporal patterns
    @Column(name = "active_hours", columnDefinition = "TEXT")
    private String activeHours = "{}";

    @Column(name = "active_days", columnDefinition = "TEXT")
    private String activeDays = "{}";

    @Column(name = "session_patterns", columnDefinition = "TEXT")
    private String sessionPatterns = "{}";

    // Transaction patterns
    @Column(name = "amount_patterns", columnDefinition = "TEXT")
    private String amountPatterns = "{}";

    @Column(name = "frequency_patterns", columnDefinition = "TEXT")
    private String frequencyPatterns = "{}";

    @Column(name = "recipient_patterns", columnDefinition = "TEXT")
    private String recipientPatterns = "{}";

    // Location patterns
    @Column(name = "location_clusters", columnDefinition = "TEXT")
    private String locationClusters = "{}";

    @Column(name = "travel_patterns", columnDefinition = "TEXT")
    private String travelPatterns = "{}";

    @Column(name = "typical_radius")
    private Double typicalRadius;

    // Device patterns
    @Column(name = "device_fingerprints", columnDefinition = "TEXT")
    private String deviceFingerprints = "{}";

    @Column(name = "device_usage_patterns", columnDefinition = "TEXT")
    private String deviceUsagePatterns = "{}";

    // Behavioral scores
    @Column(name = "regularity_score", nullable = false)
    @DecimalMin("0.0") @DecimalMax("1.0")
    private Double regularityScore = 0.0;

    @Column(name = "risk_appetite", nullable = false)
    @DecimalMin("0.0") @DecimalMax("1.0")
    private Double riskAppetite = 0.0;

    @Column(name = "trust_score", nullable = false)
    @DecimalMin("0.0") @DecimalMax("1.0")
    private Double trustScore = 0.0;

    // Analysis metadata
    @Column(name = "last_analyzed", nullable = false)
    private LocalDateTime lastAnalyzed = LocalDateTime.now();

    @Column(name = "data_points", nullable = false)
    private Integer dataPoints = 0;

    @Column(name = "confidence_level", nullable = false)
    @DecimalMin("0.0") @DecimalMax("1.0")
    private Double confidenceLevel = 0.0;

    // Constructors
    public BehavioralAnalysis() {}

    public BehavioralAnalysis(User user) {
        this.user = user;
    }

    // Business methods
    public void updateAnalysis(String newActivityData) {
        this.lastAnalyzed = LocalDateTime.now();
        this.dataPoints++;
        // Additional logic would be implemented here
    }

    public boolean isHighTrust() {
        return trustScore >= 0.8;
    }

    public boolean isRegularUser() {
        return regularityScore >= 0.7;
    }

    public boolean hasEnoughData() {
        return dataPoints >= 10 && confidenceLevel >= 0.6;
    }

    @Override
    public String toString() {
        return String.format("BehavioralAnalysis{user=%s, trustScore=%.2f, dataPoints=%d}", 
            user != null ? user.getUsername() : "null", trustScore, dataPoints);
    }
}
