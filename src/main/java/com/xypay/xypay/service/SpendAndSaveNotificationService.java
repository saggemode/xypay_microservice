package com.xypay.xypay.service;

import com.xypay.xypay.domain.SpendAndSaveAccount;
import com.xypay.xypay.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for sending Spend & Save notifications and summaries
 * Equivalent to Django's SpendAndSaveNotificationService
 */
@Service
@Slf4j
public class SpendAndSaveNotificationService {
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Send daily savings summary notification
     * Equivalent to Django's SpendAndSaveNotificationService.send_weekly_savings_summary
     */
    public void sendDailySavingsSummary(User user, SpendAndSaveAccount account, Map<String, Object> dailyStats) {
        try {
            log.info("Sending daily savings summary for user: {}", user.getUsername());
            
            // Add account-specific data to stats
            dailyStats.put("account", account);
            dailyStats.put("savings_percentage", account.getSavingsPercentage());
            dailyStats.put("current_savings_rate", account.getSavingsPercentage());
            dailyStats.put("interest_rate", account.getDailyTier1Rate().multiply(new java.math.BigDecimal("365")));
            
            // Send email notification
            emailService.sendSpendSaveDailySummary(user, dailyStats);
            
            // TODO: Add push notification, SMS, or in-app notification here
            // For now, we're just logging the notification
            log.info("Daily savings summary notification sent for user: {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error sending daily savings summary for user {}: {}", 
                user.getUsername(), e.getMessage());
        }
    }
    
    /**
     * Send weekly savings summary notification
     * Equivalent to Django's SpendAndSaveNotificationService.send_weekly_savings_summary
     */
    public void sendWeeklySavingsSummary(User user, SpendAndSaveAccount account, Map<String, Object> weeklyStats) {
        try {
            log.info("Sending weekly savings summary for user: {}", user.getUsername());
            
            // Add account-specific data to stats
            weeklyStats.put("account", account);
            weeklyStats.put("savings_percentage", account.getSavingsPercentage());
            weeklyStats.put("current_savings_rate", account.getSavingsPercentage());
            weeklyStats.put("interest_rate", account.getDailyTier1Rate().multiply(new java.math.BigDecimal("365")));
            weeklyStats.put("weekly_interest_earned", account.calculateTieredInterest().multiply(new java.math.BigDecimal("7")));
            
            // Send email notification
            emailService.sendSpendSaveWeeklySummary(user, weeklyStats);
            
            // TODO: Add push notification, SMS, or in-app notification here
            // For now, we're just logging the notification
            log.info("Weekly savings summary notification sent for user: {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error sending weekly savings summary for user {}: {}", 
                user.getUsername(), e.getMessage());
        }
    }
    
    /**
     * Send savings goal achievement notification
     */
    public void sendSavingsGoalAchievement(User user, SpendAndSaveAccount account, String goalType) {
        try {
            log.info("Sending savings goal achievement notification for user: {}", user.getUsername());
            
            Map<String, Object> notificationData = Map.of(
                "account", account,
                "goal_type", goalType,
                "achieved_at", java.time.LocalDateTime.now(),
                "current_balance", account.getBalance(),
                "savings_percentage", account.getSavingsPercentage()
            );
            
            emailService.sendNotification(
                user, 
                "Congratulations! You've achieved your savings goal!",
                "spend-save/goal_achievement",
                notificationData
            );
            
            log.info("Savings goal achievement notification sent for user: {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error sending savings goal achievement notification for user {}: {}", 
                user.getUsername(), e.getMessage());
        }
    }
    
    /**
     * Send interest rate change notification
     */
    public void sendInterestRateChangeNotification(User user, SpendAndSaveAccount account, 
                                                  java.math.BigDecimal oldRate, java.math.BigDecimal newRate) {
        try {
            log.info("Sending interest rate change notification for user: {}", user.getUsername());
            
            Map<String, Object> notificationData = Map.of(
                "account", account,
                "old_rate", oldRate,
                "new_rate", newRate,
                "change_date", java.time.LocalDateTime.now(),
                "impact_on_balance", account.getBalance().multiply(newRate.subtract(oldRate))
            );
            
            emailService.sendNotification(
                user, 
                "Your Spend & Save interest rate has been updated",
                "spend-save/rate_change",
                notificationData
            );
            
            log.info("Interest rate change notification sent for user: {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error sending interest rate change notification for user {}: {}", 
                user.getUsername(), e.getMessage());
        }
    }
    
    /**
     * Send low balance alert
     */
    public void sendLowBalanceAlert(User user, SpendAndSaveAccount account, java.math.BigDecimal threshold) {
        try {
            log.info("Sending low balance alert for user: {}", user.getUsername());
            
            Map<String, Object> notificationData = Map.of(
                "account", account,
                "current_balance", account.getBalance(),
                "threshold", threshold,
                "alert_date", java.time.LocalDateTime.now()
            );
            
            emailService.sendNotification(
                user, 
                "Low Balance Alert - Spend & Save Account",
                "spend-save/low_balance_alert",
                notificationData
            );
            
            log.info("Low balance alert sent for user: {}", user.getUsername());
            
        } catch (Exception e) {
            log.error("Error sending low balance alert for user {}: {}", 
                user.getUsername(), e.getMessage());
        }
    }
}