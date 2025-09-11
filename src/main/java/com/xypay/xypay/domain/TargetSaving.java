package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "target_savings")
public class TargetSaving extends BaseEntity {
    
    public enum Category {
        ACCOMMODATION, EDUCATION, BUSINESS, JAPA, VEHICLE, WEDDING, 
        EMERGENCY, INVESTMENT, TRAVEL, HOME_RENOVATION, MEDICAL, 
        ENTERTAINMENT, OTHER
    }
    
    public enum Frequency {
        DAILY, WEEKLY, MONTHLY
    }
    
    public enum Source {
        WALLET, XYSAVE, BOTH
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "name", length = 255)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20)
    private Category category;
    
    @Column(name = "target_amount", precision = 15, scale = 2)
    private BigDecimal targetAmount;
    
    @Column(name = "account_number", unique = true, length = 20)
    private String accountNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 10)
    private Source source = Source.WALLET;
    
    @Column(name = "strict_mode")
    private Boolean strictMode = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", length = 10)
    private Frequency frequency;
    
    @Column(name = "preferred_deposit_day", length = 20)
    private String preferredDepositDay;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "current_amount", precision = 15, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_completed")
    private Boolean isCompleted = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public TargetSaving() {}
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public BigDecimal getTargetAmount() {
        return targetAmount;
    }
    
    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public Source getSource() {
        return source;
    }
    
    public void setSource(Source source) {
        this.source = source;
    }
    
    public Boolean getStrictMode() {
        return strictMode;
    }
    
    public void setStrictMode(Boolean strictMode) {
        this.strictMode = strictMode;
    }
    
    public Frequency getFrequency() {
        return frequency;
    }
    
    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }
    
    public String getPreferredDepositDay() {
        return preferredDepositDay;
    }
    
    public void setPreferredDepositDay(String preferredDepositDay) {
        this.preferredDepositDay = preferredDepositDay;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }
    
    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }
    
    public Boolean getActive() {
        return isActive;
    }
    
    public void setActive(Boolean active) {
        isActive = active;
    }
    
    public Boolean getCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(Boolean completed) {
        isCompleted = completed;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility methods
    public BigDecimal getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentAmount.multiply(new BigDecimal("100"))
                .divide(targetAmount, 2, BigDecimal.ROUND_HALF_UP);
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
        int remaining = (int) java.time.temporal.ChronoUnit.DAYS.between(today, endDate);
        return Math.max(0, remaining);
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
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
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
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
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
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        int monthsRemaining = Math.max(1, daysRemaining / 30);
        return remaining.divide(new BigDecimal(monthsRemaining), 2, BigDecimal.ROUND_HALF_UP);
    }
}