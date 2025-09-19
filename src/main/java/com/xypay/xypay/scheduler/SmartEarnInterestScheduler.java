package com.xypay.xypay.scheduler;

import com.xypay.xypay.domain.SmartEarnAccount;
import com.xypay.xypay.domain.SmartEarnInterestHistory;
import com.xypay.xypay.repository.SmartEarnAccountRepository;
import com.xypay.xypay.repository.SmartEarnInterestHistoryRepository;
import com.xypay.xypay.service.SmartEarnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class SmartEarnInterestScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(SmartEarnInterestScheduler.class);
    
    @Autowired
    private SmartEarnAccountRepository smartEarnAccountRepository;
    
    @Autowired
    private SmartEarnInterestHistoryRepository smartEarnInterestHistoryRepository;
    
    @Autowired
    private SmartEarnService smartEarnService;
    
    /**
     * Calculate daily interest for all SmartEarn accounts
     * Runs every day at 11:00 AM
     */
    @Scheduled(cron = "0 0 11 * * *")
    @Transactional
    public void calculateDailyInterest() {
        logger.info("Starting daily SmartEarn interest calculation at {}", LocalDateTime.now());
        
        try {
            // Get all active accounts that need interest calculation
            LocalDateTime beforeTime = LocalDateTime.now().minusDays(1);
            List<SmartEarnAccount> accounts = smartEarnAccountRepository
                .findAccountsNeedingInterestCalculation(beforeTime);
            
            logger.info("Found {} SmartEarn accounts for interest calculation", accounts.size());
            
            int processedCount = 0;
            int errorCount = 0;
            
            for (SmartEarnAccount account : accounts) {
                try {
                    // Calculate interest for this account
                    calculateInterestForAccount(account);
                    processedCount++;
                    
                } catch (Exception e) {
                    logger.error("Error calculating interest for account {}: {}", 
                               account.getAccountNumber(), e.getMessage(), e);
                    errorCount++;
                }
            }
            
            logger.info("Daily SmartEarn interest calculation completed. Processed: {}, Errors: {}", 
                       processedCount, errorCount);
            
        } catch (Exception e) {
            logger.error("Error in daily SmartEarn interest calculation: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Credit accumulated interest for all accounts
     * Runs every day at 11:30 AM (30 minutes after interest calculation)
     */
    @Scheduled(cron = "0 30 11 * * *")
    @Transactional
    public void creditAccumulatedInterest() {
        logger.info("Starting SmartEarn interest crediting at {}", LocalDateTime.now());
        
        try {
            // Get all accounts with uncredited interest
            List<SmartEarnInterestHistory> uncreditedInterest = smartEarnInterestHistoryRepository
                .findInterestHistoryReadyForCrediting(LocalDate.now());
            
            logger.info("Found {} interest records ready for crediting", uncreditedInterest.size());
            
            final int[] processedCount = {0};
            final int[] errorCount = {0};
            
            // Group by account and process
            uncreditedInterest.stream()
                .collect(java.util.stream.Collectors.groupingBy(SmartEarnInterestHistory::getSmartEarnAccount))
                .forEach((account, interestList) -> {
                    try {
                        creditInterestForAccount(account, interestList);
                        processedCount[0]++;
                    } catch (Exception e) {
                        logger.error("Error crediting interest for account {}: {}", 
                                   account.getAccountNumber(), e.getMessage(), e);
                        errorCount[0]++;
                    }
                });
            
            logger.info("SmartEarn interest crediting completed. Processed: {}, Errors: {}", 
                       processedCount[0], errorCount[0]);
            
        } catch (Exception e) {
            logger.error("Error in SmartEarn interest crediting: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Calculate interest for a specific account
     */
    private void calculateInterestForAccount(SmartEarnAccount account) {
        if (account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return; // No interest to calculate
        }
        
        LocalDate today = LocalDate.now();
        
        // Check if interest already calculated for today
        if (smartEarnInterestHistoryRepository.existsBySmartEarnAccountAndInterestDate(account, today)) {
            return;
        }
        
        // Calculate daily interest
        BigDecimal dailyInterest = account.calculateDailyInterest();
        
        // Create interest history record
        SmartEarnInterestHistory interestHistory = new SmartEarnInterestHistory();
        interestHistory.setSmartEarnAccount(account);
        interestHistory.setInterestDate(today);
        interestHistory.setBalanceAtStart(account.getBalance());
        interestHistory.setBalanceAtEnd(account.getBalance());
        interestHistory.setAverageBalance(account.getBalance());
        interestHistory.setInterestRate(SmartEarnAccount.DAILY_INTEREST_RATE);
        interestHistory.setInterestEarned(dailyInterest);
        interestHistory.setIsCredited(false);
        
        smartEarnInterestHistoryRepository.save(interestHistory);
        
        // Update account's last interest calculation
        account.setLastInterestCalculation(LocalDateTime.now());
        smartEarnAccountRepository.save(account);
        
        logger.debug("Interest calculated for account {}: {}", account.getAccountNumber(), dailyInterest);
    }
    
    /**
     * Credit interest for a specific account
     */
    private void creditInterestForAccount(SmartEarnAccount account, List<SmartEarnInterestHistory> interestList) {
        if (interestList.isEmpty()) {
            return;
        }
        
        // Calculate total interest to credit
        BigDecimal totalInterest = interestList.stream()
            .map(SmartEarnInterestHistory::getInterestEarned)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalInterest.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        // Update account balance
        account.setBalance(account.getBalance().add(totalInterest));
        account.setTotalInterestEarned(account.getTotalInterestEarned().add(totalInterest));
        smartEarnAccountRepository.save(account);
        
        // Mark interest as credited
        interestList.forEach(SmartEarnInterestHistory::markAsCredited);
        smartEarnInterestHistoryRepository.saveAll(interestList);
        
        logger.debug("Interest credited for account {}: {}", account.getAccountNumber(), totalInterest);
    }
    
    /**
     * Manual trigger for interest calculation (for testing or emergency use)
     */
    @Transactional
    public void manualInterestCalculation() {
        logger.info("Manual SmartEarn interest calculation triggered at {}", LocalDateTime.now());
        calculateDailyInterest();
    }
    
    /**
     * Manual trigger for interest crediting (for testing or emergency use)
     */
    @Transactional
    public void manualInterestCrediting() {
        logger.info("Manual SmartEarn interest crediting triggered at {}", LocalDateTime.now());
        creditAccumulatedInterest();
    }
}
