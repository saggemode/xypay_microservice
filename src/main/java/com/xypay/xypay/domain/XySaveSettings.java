package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "xy_save_settings")
public class XySaveSettings extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "daily_interest_notifications")
    private Boolean dailyInterestNotifications = true;
    
    @Column(name = "goal_reminders")
    private Boolean goalReminders = true;
    
    @Column(name = "auto_save_notifications")
    private Boolean autoSaveNotifications = true;
    
    @Column(name = "investment_updates")
    private Boolean investmentUpdates = true;
    
    @Column(name = "preferred_interest_payout", length = 20)
    private String preferredInterestPayout = "daily"; // daily, weekly, monthly
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public XySaveSettings() {}
    
    // Getters and Setters
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Boolean getDailyInterestNotifications() {
        return dailyInterestNotifications;
    }
    
    public void setDailyInterestNotifications(Boolean dailyInterestNotifications) {
        this.dailyInterestNotifications = dailyInterestNotifications;
    }
    
    public Boolean getGoalReminders() {
        return goalReminders;
    }
    
    public void setGoalReminders(Boolean goalReminders) {
        this.goalReminders = goalReminders;
    }
    
    public Boolean getAutoSaveNotifications() {
        return autoSaveNotifications;
    }
    
    public void setAutoSaveNotifications(Boolean autoSaveNotifications) {
        this.autoSaveNotifications = autoSaveNotifications;
    }
    
    public Boolean getInvestmentUpdates() {
        return investmentUpdates;
    }
    
    public void setInvestmentUpdates(Boolean investmentUpdates) {
        this.investmentUpdates = investmentUpdates;
    }
    
    public String getPreferredInterestPayout() {
        return preferredInterestPayout;
    }
    
    public void setPreferredInterestPayout(String preferredInterestPayout) {
        this.preferredInterestPayout = preferredInterestPayout;
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
}