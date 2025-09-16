package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "xysave_settings")
public class XySaveSettings {
    
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
    
    @Column(name = "daily_interest_notifications", nullable = false)
    private Boolean dailyInterestNotifications = true;
    
    @Column(name = "goal_reminders", nullable = false)
    private Boolean goalReminders = true;
    
    @Column(name = "auto_save_notifications", nullable = false)
    private Boolean autoSaveNotifications = true;
    
    @Column(name = "investment_updates", nullable = false)
    private Boolean investmentUpdates = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_interest_payout", length = 20, nullable = false)
    private InterestPayoutFrequency preferredInterestPayout = InterestPayoutFrequency.DAILY;
    
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