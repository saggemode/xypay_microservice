package com.xypay.xypay.config;

import com.xypay.xypay.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "transaction_rule_configurations")
public class TransactionRuleConfiguration extends BaseEntity {
    
    @Column(name = "rule_name")
    private String ruleName;
    
    @Column(name = "rule_type")
    private String ruleType; // APPROVAL, LIMIT, BLOCK, FRAUD_CHECK
    
    @Column(name = "transaction_type")
    private String transactionType; // TRANSFER, WITHDRAWAL, DEPOSIT, etc.
    
    @Column(name = "approval_threshold")
    private BigDecimal approvalThreshold;
    
    @Column(name = "daily_limit")
    private BigDecimal dailyLimit;
    
    @Column(name = "blocked_countries")
    private String blockedCountries; // JSON format list of blocked countries
    
    @Column(name = "fraud_detection_params")
    private String fraudDetectionParams; // JSON format for fraud detection parameters
    
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Explicit getter and setter for isActive field
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}