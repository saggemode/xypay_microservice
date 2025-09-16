package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "spend_and_save_settings")
public class SpendAndSaveSettings {
    
    public enum WithdrawalDestination {
        WALLET("wallet", "Wallet"),
        XYSAVE("xysave", "XySave Account");
        
        private final String code;
        private final String description;
        
        WithdrawalDestination(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum FundingPreference {
        AUTO("auto", "Auto (prefer XySave, fallback Wallet)"),
        XYSAVE("xysave", "XySave (fallback Wallet)"),
        WALLET("wallet", "Wallet only");
        
        private final String code;
        private final String description;
        
        FundingPreference(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum InterestPayoutFrequency {
        DAILY("daily", "Daily"),
        WEEKLY("weekly", "Weekly"),
        MONTHLY("monthly", "Monthly");
        
        private final String code;
        private final String description;
        
        InterestPayoutFrequency(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "auto_save_notifications", nullable = false)
    private Boolean autoSaveNotifications = true;
    
    @Column(name = "interest_notifications", nullable = false)
    private Boolean interestNotifications = true;
    
    @Column(name = "withdrawal_notifications", nullable = false)
    private Boolean withdrawalNotifications = true;
    
    @Column(name = "preferred_savings_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal preferredSavingsPercentage = new BigDecimal("5.00");
    
    @Column(name = "min_transaction_threshold", precision = 19, scale = 4, nullable = false)
    private BigDecimal minTransactionThreshold = new BigDecimal("100.00");
    
    @Enumerated(EnumType.STRING)
    @Column(name = "default_withdrawal_destination", length = 20, nullable = false)
    private WithdrawalDestination defaultWithdrawalDestination = WithdrawalDestination.WALLET;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "funding_preference", length = 10, nullable = false)
    private FundingPreference fundingPreference = FundingPreference.AUTO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "interest_payout_frequency", length = 20, nullable = false)
    private InterestPayoutFrequency interestPayoutFrequency = InterestPayoutFrequency.DAILY;
    
    @Column(name = "auto_withdrawal_enabled", nullable = false)
    private Boolean autoWithdrawalEnabled = false;
    
    @Column(name = "auto_withdrawal_threshold", precision = 19, scale = 4)
    private BigDecimal autoWithdrawalThreshold;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}