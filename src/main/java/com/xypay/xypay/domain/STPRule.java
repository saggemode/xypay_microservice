package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stp_rules")
public class STPRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Column(name = "rule_type")
    private String ruleType; // TRANSACTION_LIMIT, KYC_STATUS, CHANNEL_TYPE, TIME_BASED, AMOUNT_RANGE

    @Column(name = "entity_type")
    private String entityType; // TRANSACTION, PAYMENT, TRANSFER, LOAN_APPLICATION

    @Column(name = "condition_field")
    private String conditionField; // amount, channel, kycStatus, userTier, etc.

    @Column(name = "condition_operator")
    private String conditionOperator; // LT, LE, GT, GE, EQ, IN, BETWEEN

    @Column(name = "condition_value")
    private String conditionValue;

    @Column(name = "min_amount")
    private BigDecimal minAmount;

    @Column(name = "max_amount")
    private BigDecimal maxAmount;

    @Column(name = "allowed_channels")
    private String allowedChannels; // Comma-separated list

    @Column(name = "required_kyc_level")
    private String requiredKycLevel;

    @Column(name = "auto_approve")
    private Boolean autoApprove = false;

    @Column(name = "skip_manual_review")
    private Boolean skipManualReview = false;

    @Column(name = "priority")
    private Integer priority = 1; // Higher number = higher priority

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
