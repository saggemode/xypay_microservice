package com.xypay.xypay.scheduler;

import com.xypay.xypay.service.SpendAndSaveInterestService;
import com.xypay.xypay.service.XySaveInterestService;
import com.xypay.xypay.service.FixedSavingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduler for processing daily interest calculations and payouts
 * Equivalent to Django's process_daily_interest Celery task
 */
@Component
@Slf4j
public class DailyInterestScheduler {
    
    @Autowired
    private SpendAndSaveInterestService spendAndSaveInterestService;
    
    @Autowired
    private XySaveInterestService xySaveInterestService;
    
    // @Autowired
    // private FixedSavingsService fixedSavingsService; // Not used - handled by FixedSavingsMaturityProcessor
    
    /**
     * Process daily interest for Spend & Save and XySave accounts
     * Runs daily at 2 AM
     * Equivalent to Django's @shared_task(bind=True, ignore_result=True) process_daily_interest
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void processDailyInterest() {
        log.info("Starting daily interest processing...");
        
        try {
            // Process Spend & Save interest
            try {
                int spendSaveProcessed = spendAndSaveInterestService.processDailyInterestPayout();
                log.info("Processed Spend & Save interest for {} accounts", spendSaveProcessed);
            } catch (Exception e) {
                log.error("Error processing Spend & Save interest: {}", e.getMessage());
                // Continue processing other services even if one fails
            }
            
            // Process XySave interest
            try {
                xySaveInterestService.calculateDailyInterestForAllAccounts();
                log.info("XySave interest processing completed");
            } catch (Exception e) {
                log.error("Error processing XySave interest: {}", e.getMessage());
                // Continue processing other services even if one fails
            }
            
            // Note: Fixed Savings maturity processing is handled by FixedSavingsMaturityProcessor
            // which runs separately at 1 AM daily
            log.info("Fixed savings processing is handled by FixedSavingsMaturityProcessor");
            
            log.info("Daily interest processing completed successfully");
            
        } catch (Exception e) {
            log.error("Error during daily interest processing: {}", e.getMessage());
        }
    }
    
    /**
     * Alternative method for APScheduler compatibility
     * Equivalent to Django's run_daily_interest_job function
     */
    @Transactional
    public void runDailyInterestJob() {
        log.info("Running daily interest job (APScheduler compatible)...");
        processDailyInterest();
    }
}
