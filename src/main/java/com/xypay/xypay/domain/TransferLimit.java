package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transfer_limits", indexes = {
    @Index(name = "idx_transfer_limit_user", columnList = "user_id"),
    @Index(name = "idx_transfer_limit_type", columnList = "limit_type"),
    @Index(name = "idx_transfer_limit_status", columnList = "is_active")
})
public class TransferLimit extends BaseEntity {
    
    public enum LimitType {
        DAILY("daily"),
        WEEKLY("weekly"),
        MONTHLY("monthly"),
        YEARLY("yearly"),
        PER_TRANSACTION("per_transaction"),
        CUMULATIVE("cumulative");
        
        private final String value;
        
        LimitType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    public enum LimitCategory {
        INTERNAL_TRANSFER("internal_transfer"),
        EXTERNAL_TRANSFER("external_transfer"),
        BULK_TRANSFER("bulk_transfer"),
        INTERNATIONAL_TRANSFER("international_transfer"),
        BILL_PAYMENT("bill_payment"),
        CASH_WITHDRAWAL("cash_withdrawal"),
        ALL_TRANSACTIONS("all_transactions");
        
        private final String value;
        
        LimitCategory(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type", nullable = false)
    private LimitType limitType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "limit_category", nullable = false)
    private LimitCategory limitCategory;
    
    @Column(name = "limit_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal limitAmount;
    
    @Column(name = "used_amount", precision = 19, scale = 4)
    private BigDecimal usedAmount = BigDecimal.ZERO;
    
    @Column(name = "remaining_amount", precision = 19, scale = 4)
    private BigDecimal remainingAmount;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;
    
    @Column(name = "effective_until")
    private LocalDateTime effectiveUntil;
    
    @Column(name = "last_reset_date")
    private LocalDateTime lastResetDate;
    
    @Column(name = "next_reset_date")
    private LocalDateTime nextResetDate;
    
    @Column(name = "auto_reset_enabled", nullable = false)
    private Boolean autoResetEnabled = true;
    
    @Column(name = "override_allowed", nullable = false)
    private Boolean overrideAllowed = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;
    
    @Column(name = "approval_required", nullable = false)
    private Boolean approvalRequired = false;
    
    @Column(name = "notification_threshold", precision = 5, scale = 2)
    private BigDecimal notificationThreshold = new BigDecimal("80.00"); // 80%
    
    @Column(name = "last_notification_sent")
    private LocalDateTime lastNotificationSent;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON metadata
    
    // Constructors
    public TransferLimit() {}
    
    public TransferLimit(User user, LimitType limitType, LimitCategory limitCategory, 
                        BigDecimal limitAmount, LocalDateTime effectiveFrom) {
        this.user = user;
        this.limitType = limitType;
        this.limitCategory = limitCategory;
        this.limitAmount = limitAmount;
        this.effectiveFrom = effectiveFrom;
        this.remainingAmount = limitAmount;
        this.lastResetDate = effectiveFrom;
        this.nextResetDate = calculateNextResetDate(effectiveFrom, limitType);
    }
    
    // Business methods
    public boolean canTransfer(BigDecimal amount) {
        if (!isActive) return false;
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(effectiveFrom) || 
            (effectiveUntil != null && now.isAfter(effectiveUntil))) {
            return false;
        }
        
        return remainingAmount.compareTo(amount) >= 0;
    }
    
    public void useLimit(BigDecimal amount) {
        this.usedAmount = this.usedAmount.add(amount);
        this.remainingAmount = this.limitAmount.subtract(this.usedAmount);
    }
    
    public void resetLimit() {
        this.usedAmount = BigDecimal.ZERO;
        this.remainingAmount = this.limitAmount;
        this.lastResetDate = LocalDateTime.now();
        this.nextResetDate = calculateNextResetDate(LocalDateTime.now(), limitType);
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public boolean isExpired() {
        return effectiveUntil != null && LocalDateTime.now().isAfter(effectiveUntil);
    }
    
    public boolean needsReset() {
        return autoResetEnabled && 
               nextResetDate != null && 
               LocalDateTime.now().isAfter(nextResetDate);
    }
    
    public boolean shouldSendNotification() {
        if (lastNotificationSent != null && 
            lastNotificationSent.plusHours(24).isAfter(LocalDateTime.now())) {
            return false; // Don't send notification if sent within last 24 hours
        }
        
        BigDecimal usagePercentage = usedAmount
                .divide(limitAmount, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return usagePercentage.compareTo(notificationThreshold) >= 0;
    }
    
    public void markNotificationSent() {
        this.lastNotificationSent = LocalDateTime.now();
    }
    
    public BigDecimal getUsagePercentage() {
        if (limitAmount.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        
        return usedAmount
                .divide(limitAmount, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    private LocalDateTime calculateNextResetDate(LocalDateTime from, LimitType limitType) {
        switch (limitType) {
            case DAILY:
                return from.plusDays(1);
            case WEEKLY:
                return from.plusWeeks(1);
            case MONTHLY:
                return from.plusMonths(1);
            case YEARLY:
                return from.plusYears(1);
            case PER_TRANSACTION:
            case CUMULATIVE:
            default:
                return null; // No reset for these types
        }
    }
}
