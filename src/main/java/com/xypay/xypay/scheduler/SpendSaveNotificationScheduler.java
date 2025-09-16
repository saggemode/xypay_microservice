package com.xypay.xypay.scheduler;

import com.xypay.xypay.domain.SpendAndSaveAccount;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.SpendAndSaveAccountRepository;
import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.service.SpendAndSaveNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Scheduler for sending daily Spend & Save summaries and notifications
 * Equivalent to Django's send_spend_save_daily_summaries Celery task
 */
@Component
@Slf4j
public class SpendSaveNotificationScheduler {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SpendAndSaveAccountRepository spendAndSaveAccountRepository;
    
    @Autowired
    private SpendAndSaveNotificationService spendAndSaveNotificationService;
    
    /**
     * Send daily Spend & Save summaries/notifications
     * Runs daily at 8 AM
     * Equivalent to Django's @shared_task(bind=True, ignore_result=True) send_spend_save_daily_summaries
     */
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void sendSpendSaveDailySummaries() {
        log.info("Starting Spend & Save daily summary processing...");
        
        try {
            // Get all active users
            List<User> activeUsers = userRepository.findAll().stream()
                .filter(User::isEnabled)
                .toList();
            
            int processedCount = 0;
            int skippedCount = 0;
            
            for (User user : activeUsers) {
                try {
                    // Get user's Spend & Save account
                    Optional<SpendAndSaveAccount> accountOpt = spendAndSaveAccountRepository.findByUser(user);
                    if (accountOpt.isEmpty()) {
                        skippedCount++;
                        continue;
                    }
                    
                    SpendAndSaveAccount account = accountOpt.get();
                    
                    // Prepare daily statistics
                    Map<String, Object> dailyStats = new HashMap<>();
                    dailyStats.put("total_spent", BigDecimal.ZERO); // This would be calculated from actual spending
                    dailyStats.put("total_saved", account.getTotalSavedFromSpending() != null ? 
                        account.getTotalSavedFromSpending() : BigDecimal.ZERO);
                    dailyStats.put("transactions_count", account.getTotalTransactionsProcessed() != null ? 
                        account.getTotalTransactionsProcessed() : 0);
                    dailyStats.put("current_balance", account.getBalance());
                    dailyStats.put("interest_earned_today", account.calculateTieredInterest());
                    
                    // Send daily summary notification
                    spendAndSaveNotificationService.sendDailySavingsSummary(user, account, dailyStats);
                    
                    processedCount++;
                    log.info("Sent daily Spend & Save summary for user: {}", user.getUsername());
                    
                } catch (Exception e) {
                    log.error("Error processing daily summary for user {}: {}", 
                        user.getUsername(), e.getMessage());
                    skippedCount++;
                }
            }
            
            log.info("Spend & Save daily summary processing completed. Processed: {}, Skipped: {}", 
                processedCount, skippedCount);
            
        } catch (Exception e) {
            log.error("Error during Spend & Save daily summary processing: {}", e.getMessage());
        }
    }
    
    /**
     * Send weekly Spend & Save summaries
     * Runs every Monday at 10 AM
     */
    @Scheduled(cron = "0 0 10 * * MON")
    @Transactional
    public void sendSpendSaveWeeklySummaries() {
        log.info("Starting Spend & Save weekly summary processing...");
        
        try {
            // Get all active users with Spend & Save accounts
            List<SpendAndSaveAccount> accounts = spendAndSaveAccountRepository.findByIsActiveTrue();
            
            int processedCount = 0;
            
            for (SpendAndSaveAccount account : accounts) {
                try {
                    // Prepare weekly statistics
                    Map<String, Object> weeklyStats = new HashMap<>();
                    weeklyStats.put("total_spent", BigDecimal.ZERO); // This would be calculated from actual spending
                    weeklyStats.put("total_saved", account.getTotalSavedFromSpending() != null ? 
                        account.getTotalSavedFromSpending() : BigDecimal.ZERO);
                    weeklyStats.put("transactions_count", account.getTotalTransactionsProcessed() != null ? 
                        account.getTotalTransactionsProcessed() : 0);
                    weeklyStats.put("current_balance", account.getBalance());
                    weeklyStats.put("weekly_interest_earned", account.calculateTieredInterest().multiply(new BigDecimal("7")));
                    
                    // Send weekly summary notification
                    spendAndSaveNotificationService.sendWeeklySavingsSummary(account.getUser(), account, weeklyStats);
                    
                    processedCount++;
                    log.info("Sent weekly Spend & Save summary for user: {}", account.getUser().getUsername());
                    
                } catch (Exception e) {
                    log.error("Error processing weekly summary for account {}: {}", 
                        account.getId(), e.getMessage());
                }
            }
            
            log.info("Spend & Save weekly summary processing completed. Processed: {}", processedCount);
            
        } catch (Exception e) {
            log.error("Error during Spend & Save weekly summary processing: {}", e.getMessage());
        }
    }
}
