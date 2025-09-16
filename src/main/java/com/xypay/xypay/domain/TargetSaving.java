package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "target_savings")
public class TargetSaving {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "name", length = 255, nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20, nullable = false)
    private TargetSavingCategory category;
    
    @Column(name = "target_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal targetAmount;
    
    @Column(name = "account_number", length = 20, unique = true, nullable = false)
    private String accountNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 10, nullable = false)
    private TargetSavingSource source = TargetSavingSource.WALLET;
    
    @Column(name = "strict_mode", nullable = false)
    private Boolean strictMode = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", length = 10, nullable = false)
    private TargetSavingFrequency frequency;
    
    @Column(name = "preferred_deposit_day", length = 20)
    private String preferredDepositDay;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "current_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal currentAmount = BigDecimal.ZERO;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "targetSaving", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TargetSavingDeposit> deposits;
    
    @OneToMany(mappedBy = "targetSaving", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TargetSavingWithdrawal> withdrawals;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        
        if (accountNumber == null) {
            accountNumber = generateAccountNumber();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private String generateAccountNumber() {
        String userId = String.format("%08d", user.getId());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "TS" + userId + uuid;
    }
    
    public BigDecimal getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return currentAmount.divide(targetAmount, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
    }
    
    public BigDecimal getRemainingAmount() {
        if (targetAmount == null) {
            return BigDecimal.ZERO;
        }
        return targetAmount.subtract(currentAmount).max(BigDecimal.ZERO);
    }
    
    public int getDaysRemaining() {
        if (endDate == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        return Math.max(0, (int) java.time.temporal.ChronoUnit.DAYS.between(today, endDate));
    }
    
    public boolean isOverdue() {
        if (endDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(endDate) && !isCompleted;
    }
    
    public BigDecimal getDailyTarget() {
        int daysRemaining = getDaysRemaining();
        if (daysRemaining == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal remaining = getRemainingAmount();
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return remaining.divide(new BigDecimal(daysRemaining), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    public BigDecimal getWeeklyTarget() {
        int daysRemaining = getDaysRemaining();
        if (daysRemaining == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal remaining = getRemainingAmount();
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        int weeksRemaining = Math.max(1, daysRemaining / 7);
        return remaining.divide(new BigDecimal(weeksRemaining), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    public BigDecimal getMonthlyTarget() {
        int daysRemaining = getDaysRemaining();
        if (daysRemaining == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal remaining = getRemainingAmount();
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        int monthsRemaining = Math.max(1, daysRemaining / 30);
        return remaining.divide(new BigDecimal(monthsRemaining), 2, BigDecimal.ROUND_HALF_UP);
    }
}