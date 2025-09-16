package com.xypay.xypay.scheduler;

import com.xypay.xypay.domain.FixedSavingsAccount;
import com.xypay.xypay.repository.FixedSavingsAccountRepository;
import com.xypay.xypay.service.FixedSavingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class FixedSavingsMaturityProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(FixedSavingsMaturityProcessor.class);
    
    @Autowired
    private FixedSavingsAccountRepository fixedSavingsAccountRepository;
    
    @Autowired
    private FixedSavingsService fixedSavingsService;
    
    /**
     * Process matured fixed savings accounts daily at 1 AM
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processMaturedFixedSavings() {
        logger.info("Starting fixed savings maturity processing...");
        
        try {
            // Find accounts that should be matured but aren't marked as such
            List<FixedSavingsAccount> accountsToProcess = fixedSavingsAccountRepository
                .findByIsMaturedFalseAndPaybackDateBefore(LocalDate.now());
            
            logger.info("Found {} fixed savings accounts to process for maturity", accountsToProcess.size());
            
            for (FixedSavingsAccount account : accountsToProcess) {
                try {
                    if (account.isMature() && !account.getIsMatured()) {
                        logger.info("Processing maturity for fixed savings account: {}", account.getId());
                        
                        // Mark as matured
                        account.markAsMatured();
                        fixedSavingsAccountRepository.save(account);
                        
                        // Process payout or auto-renewal
                        if (!account.getAutoRenewalEnabled()) {
                            // Process payout
                            boolean payoutSuccess = fixedSavingsService.processMaturityPayout(account);
                            if (payoutSuccess) {
                                logger.info("Successfully processed payout for account: {}", account.getId());
                            } else {
                                logger.error("Failed to process payout for account: {}", account.getId());
                            }
                        } else {
                            // Process auto-renewal
                            try {
                                fixedSavingsService.processAutoRenewal(account);
                                logger.info("Successfully processed auto-renewal for account: {}", account.getId());
                            } catch (Exception e) {
                                logger.error("Failed to process auto-renewal for account {}: {}", 
                                    account.getId(), e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing matured fixed savings account {}: {}", 
                        account.getId(), e.getMessage());
                }
            }
            
            logger.info("Fixed savings maturity processing completed");
        } catch (Exception e) {
            logger.error("Error during fixed savings maturity processing: {}", e.getMessage());
        }
    }
    
    /**
     * Send maturity reminder notifications daily at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendMaturityReminders() {
        logger.info("Starting fixed savings maturity reminder processing...");
        
        try {
            // Find accounts that will mature in 7 days or less
            LocalDate reminderDate = LocalDate.now().plusDays(7);
            List<FixedSavingsAccount> accountsForReminder = fixedSavingsAccountRepository
                .findByIsMaturedFalseAndPaybackDateBetween(LocalDate.now(), reminderDate);
            
            logger.info("Found {} fixed savings accounts for maturity reminders", accountsForReminder.size());
            
            for (FixedSavingsAccount account : accountsForReminder) {
                try {
                    // Send reminder notification
                    fixedSavingsService.sendMaturityReminderNotification(account);
                    logger.info("Sent maturity reminder for account: {}", account.getId());
                } catch (Exception e) {
                    logger.error("Error sending maturity reminder for account {}: {}", 
                        account.getId(), e.getMessage());
                }
            }
            
            logger.info("Fixed savings maturity reminder processing completed");
        } catch (Exception e) {
            logger.error("Error during fixed savings maturity reminder processing: {}", e.getMessage());
        }
    }
}
