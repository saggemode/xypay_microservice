package com.xypay.xypay.service;

import com.xypay.xypay.domain.SpendAndSaveAccount;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.dto.InterestForecastDTO;
import com.xypay.xypay.repository.SpendAndSaveAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for handling interest calculations and payouts
 */
@Service
@Slf4j
public class SpendAndSaveInterestService {
    
    @Autowired
    private SpendAndSaveAccountRepository spendAndSaveAccountRepository;
    
    @Autowired
    private SpendAndSaveService spendAndSaveService;
    
    /**
     * Process daily interest payout for all active Spend and Save accounts
     * This should be called by a scheduled task (e.g., cron job)
     */
    @Transactional
    public int processDailyInterestPayout() {
        try {
            var activeAccounts = spendAndSaveAccountRepository.findByIsActiveTrue();
            int processedCount = 0;
            
            for (SpendAndSaveAccount account : activeAccounts) {
                try {
                    var interestTx = spendAndSaveService.calculateAndCreditInterest(account.getUser());
                    if (interestTx != null) {
                        processedCount++;
                    }
                } catch (Exception e) {
                    log.error("Error processing interest for account {}: {}", account.getId(), e.getMessage());
                    continue;
                }
            }
            
            log.info("Processed daily interest payout for {} accounts", processedCount);
            return processedCount;
            
        } catch (Exception e) {
            log.error("Error processing daily interest payout: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get interest forecast for the next specified number of days
     */
    public InterestForecastDTO getInterestForecast(User user, int days) {
        try {
            Optional<SpendAndSaveAccount> accountOpt = spendAndSaveAccountRepository.findByUser(user);
            if (accountOpt.isEmpty()) {
                return null;
            }
            
            SpendAndSaveAccount account = accountOpt.get();
            
            if (account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                InterestForecastDTO forecast = new InterestForecastDTO();
                forecast.setDailyInterest(BigDecimal.ZERO);
                forecast.setMonthlyInterest(BigDecimal.ZERO);
                forecast.setAnnualInterest(BigDecimal.ZERO);
                forecast.setForecastDays(days);
                return forecast;
            }
            
            // Calculate daily interest
            BigDecimal dailyInterest = account.calculateTieredInterest();
            
            // Calculate monthly and annual
            BigDecimal monthlyInterest = dailyInterest.multiply(new BigDecimal("30"));
            BigDecimal annualInterest = dailyInterest.multiply(new BigDecimal("365"));
            
            InterestForecastDTO forecast = new InterestForecastDTO();
            forecast.setDailyInterest(dailyInterest);
            forecast.setMonthlyInterest(monthlyInterest);
            forecast.setAnnualInterest(annualInterest);
            forecast.setForecastDays(days);
            forecast.setCurrentBalance(account.getBalance());
            
            // Parse interest breakdown
            Map<String, Object> breakdown = parseInterestBreakdown(account.getInterestBreakdown());
            forecast.setInterestBreakdown(breakdown);
            
            return forecast;
            
        } catch (Exception e) {
            log.error("Error getting interest forecast for user {}: {}", user.getUsername(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse the interest breakdown JSON string into a Map
     */
    private Map<String, Object> parseInterestBreakdown(String breakdownJson) {
        Map<String, Object> breakdown = new HashMap<>();
        try {
            // Simple JSON parsing - in a real implementation, you'd use a JSON library
            // For now, return a basic structure
            breakdown.put("tier_1", Map.of(
                "amount", 0,
                "rate", 20,
                "interest", 0
            ));
            breakdown.put("tier_2", Map.of(
                "amount", 0,
                "rate", 16,
                "interest", 0
            ));
            breakdown.put("tier_3", Map.of(
                "amount", 0,
                "rate", 8,
                "interest", 0
            ));
            breakdown.put("total_interest", 0);
        } catch (Exception e) {
            log.warn("Error parsing interest breakdown: {}", e.getMessage());
        }
        return breakdown;
    }
}
