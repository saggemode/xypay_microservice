package com.xypay.xypay.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "fraud_detection", indexes = {
    @Index(name = "idx_fraud_user_flag", columnList = "user_id, flag"),
    @Index(name = "idx_fraud_risk_score", columnList = "risk_score"),
    @Index(name = "idx_fraud_created_at", columnList = "created_at")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FraudDetection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id")
    private BankTransfer transfer;

    @Column(name = "fraud_type", length = 50, nullable = false)
    private String fraudType;

    @Column(name = "risk_score", nullable = false)
    @Min(0) @Max(100)
    private Integer riskScore;

    @Column(name = "flag", length = 20, nullable = false)
    private String flag = "NORMAL"; // NORMAL, LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_id")
    private User resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    // Constructors
    public FraudDetection() {}

    public FraudDetection(User user, String fraudType, Integer riskScore, String description) {
        this.user = user;
        this.fraudType = fraudType;
        this.riskScore = riskScore;
        this.description = description;
    }

    // Business methods
    public void resolve(User resolvedBy, String resolutionNotes) {
        this.isResolved = true;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = resolutionNotes;
    }

    public boolean isHighRisk() {
        return riskScore >= 70;
    }

    public boolean isCritical() {
        return "CRITICAL".equals(flag) || riskScore >= 90;
    }

    @Override
    public String toString() {
        return String.format("FraudDetection{user=%s, type=%s, score=%d, flag=%s}", 
            user != null ? user.getUsername() : "null", fraudType, riskScore, flag);
    }
}
