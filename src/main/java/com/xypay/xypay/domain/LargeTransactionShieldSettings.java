package com.xypay.xypay.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "large_tx_shield_settings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LargeTransactionShieldSettings extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;

    @Column(name = "per_transaction_limit", precision = 19, scale = 2)
    private BigDecimal perTransactionLimit;

    @Column(name = "daily_limit", precision = 19, scale = 2)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", precision = 19, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "face_template_hash", length = 128)
    private String faceTemplateHash;

    @Column(name = "face_template_alg", length = 20)
    private String faceTemplateAlg = "sha256";

    @Column(name = "face_registered_at")
    private LocalDateTime faceRegisteredAt;

    // Constructors
    public LargeTransactionShieldSettings() {}

    public LargeTransactionShieldSettings(User user) {
        this.user = user;
    }

    // Business methods
    public void enableShield(BigDecimal perTransactionLimit, BigDecimal dailyLimit, BigDecimal monthlyLimit) {
        this.enabled = true;
        this.perTransactionLimit = perTransactionLimit;
        this.dailyLimit = dailyLimit;
        this.monthlyLimit = monthlyLimit;
    }

    public void disableShield() {
        this.enabled = false;
    }

    public void registerFace(String faceTemplateHash) {
        this.faceTemplateHash = faceTemplateHash;
        this.faceRegisteredAt = LocalDateTime.now();
    }

    public boolean requiresVerification(BigDecimal amount) {
        if (!enabled) {
            return false;
        }
        
        return perTransactionLimit != null && amount.compareTo(perTransactionLimit) > 0;
    }

    public boolean hasFaceRegistered() {
        return faceTemplateHash != null && faceRegisteredAt != null;
    }

    @Override
    public String toString() {
        return String.format("LargeTransactionShieldSettings{user=%s, enabled=%s, perTxLimit=%s}", 
            user != null ? user.getUsername() : "null", enabled, perTransactionLimit);
    }
}
