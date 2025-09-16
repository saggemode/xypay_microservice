package com.xypay.xypay.service;

import com.xypay.xypay.domain.XySaveAccount;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.XySaveAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class XySaveInterestService {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveInterestService.class);
    
    @Autowired
    private XySaveAccountRepository xySaveAccountRepository;
    
    @Autowired
    private XySaveAccountService xySaveAccountService;
    
    @Autowired
    private XySaveTransactionService xySaveTransactionService;
    
    /**
     * Calculate and credit daily interest for all active accounts
     * This method is scheduled to run daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void calculateDailyInterestForAllAccounts() {
        try {
            List<XySaveAccount> activeAccounts = xySaveAccountRepository.findAll().stream()
                .filter(account -> Boolean.TRUE.equals(account.getIsActive()) && 
                        account.getBalance().compareTo(BigDecimal.ZERO) > 0)
                .toList();
            
            int processedCount = 0;
            for (XySaveAccount account : activeAccounts) {
                try {
                    BigDecimal dailyInterest = account.calculateDailyInterest();
                    
                    if (dailyInterest.compareTo(BigDecimal.ZERO) > 0) {
                        xySaveTransactionService.creditInterest(
                            account.getUser(),
                            dailyInterest,
                            String.format("Daily interest credit (%.2f%% p.a.)", 
                                account.getAnnualInterestRate())
                        );
                        processedCount++;
                    }
                    
                } catch (Exception e) {
                    logger.error("Error calculating interest for account {}: {}", 
                        account.getAccountNumber(), e.getMessage());
                    continue;
                }
            }
            
            logger.info("Processed daily interest for {} accounts", processedCount);
            
        } catch (Exception e) {
            logger.error("Error in daily interest calculation: {}", e.getMessage());
        }
    }
    
    /**
     * Get interest forecast for user
     */
    public Map<String, Object> getInterestForecast(User user, int days) {
        try {
            XySaveAccount account = xySaveAccountService.getXySaveAccount(user);
            
            BigDecimal dailyInterest = account.calculateDailyInterest();
            BigDecimal weeklyInterest = dailyInterest.multiply(new BigDecimal("7"));
            BigDecimal monthlyInterest = dailyInterest.multiply(new BigDecimal("30"));
            BigDecimal yearlyInterest = dailyInterest.multiply(new BigDecimal("365"));
            
            Map<String, Object> forecast = new HashMap<>();
            forecast.put("daily_interest", dailyInterest);
            forecast.put("weekly_interest", weeklyInterest);
            forecast.put("monthly_interest", monthlyInterest);
            forecast.put("yearly_interest", yearlyInterest);
            forecast.put("annual_rate", account.getAnnualInterestRate());
            forecast.put("current_balance", account.getBalance());
            forecast.put("total_interest_earned", account.getTotalInterestEarned());
            
            return forecast;
            
        } catch (Exception e) {
            logger.error("Error getting interest forecast for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Calculate interest for a specific account
     */
    @Transactional
    public BigDecimal calculateInterestForAccount(XySaveAccount account) {
        try {
            BigDecimal dailyInterest = account.calculateDailyInterest();
            
            if (dailyInterest.compareTo(BigDecimal.ZERO) > 0) {
                // Credit the interest
                xySaveTransactionService.creditInterest(
                    account.getUser(),
                    dailyInterest,
                    String.format("Interest calculation (%.2f%% p.a.)", 
                        account.getAnnualInterestRate())
                );
            }
            
            return dailyInterest;
            
        } catch (Exception e) {
            logger.error("Error calculating interest for account {}: {}", 
                account.getAccountNumber(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get interest breakdown for account
     */
    public Map<String, Object> getInterestBreakdown(User user) {
        try {
            XySaveAccount account = xySaveAccountService.getXySaveAccount(user);
            
            Map<String, Object> breakdown = new HashMap<>();
            breakdown.put("account_number", account.getAccountNumber());
            breakdown.put("current_balance", account.getBalance());
            breakdown.put("daily_interest_rate", account.getDailyInterestRate());
            breakdown.put("annual_interest_rate", account.getAnnualInterestRate());
            breakdown.put("daily_interest", account.calculateDailyInterest());
            breakdown.put("total_interest_earned", account.getTotalInterestEarned());
            breakdown.put("last_interest_calculation", account.getLastInterestCalculation());
            breakdown.put("interest_breakdown_json", account.getInterestBreakdown());
            
            return breakdown;
            
        } catch (Exception e) {
            logger.error("Error getting interest breakdown for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get interest history for user
     */
    public List<Map<String, Object>> getInterestHistory(User user, int limit) {
        try {
            XySaveAccount account = xySaveAccountService.getXySaveAccount(user);
            
            return account.getTransactions().stream()
                .filter(t -> t.getTransactionType() == com.xypay.xypay.domain.XySaveTransaction.TransactionType.INTEREST_CREDIT)
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .limit(limit)
                .map(transaction -> {
                    Map<String, Object> history = new HashMap<>();
                    history.put("amount", transaction.getAmount());
                    history.put("date", transaction.getCreatedAt());
                    history.put("description", transaction.getDescription());
                    history.put("balance_after", transaction.getBalanceAfter());
                    return history;
                })
                .toList();
                
        } catch (Exception e) {
            logger.error("Error getting interest history for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Calculate projected interest for a given amount and time period
     */
    public Map<String, Object> calculateProjectedInterest(BigDecimal amount, int days) {
        try {
            // Use the standard tiered interest rates
            BigDecimal dailyInterest = calculateTieredDailyInterest(amount);
            BigDecimal totalInterest = dailyInterest.multiply(new BigDecimal(days));
            
            Map<String, Object> projection = new HashMap<>();
            projection.put("principal_amount", amount);
            projection.put("time_period_days", days);
            projection.put("daily_interest", dailyInterest);
            projection.put("total_interest", totalInterest);
            projection.put("maturity_amount", amount.add(totalInterest));
            projection.put("effective_annual_rate", dailyInterest.multiply(new BigDecimal("365"))
                .divide(amount, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100")));
            
            return projection;
            
        } catch (Exception e) {
            logger.error("Error calculating projected interest: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Calculate tiered daily interest for a given amount
     */
    private BigDecimal calculateTieredDailyInterest(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal remainingBalance = amount;
        
        // Tier 1: First 10,000 at 20% p.a
        BigDecimal tier1Threshold = new BigDecimal("10000");
        if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier1Amount = remainingBalance.min(tier1Threshold);
            BigDecimal tier1Rate = new BigDecimal("0.20").divide(new BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP);
            totalInterest = totalInterest.add(tier1Amount.multiply(tier1Rate));
            remainingBalance = remainingBalance.subtract(tier1Amount);
        }
        
        // Tier 2: Next 90,000 (10,001 - 100,000) at 16% p.a
        BigDecimal tier2Threshold = new BigDecimal("100000");
        if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier2Amount = remainingBalance.min(tier2Threshold.subtract(tier1Threshold));
            BigDecimal tier2Rate = new BigDecimal("0.16").divide(new BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP);
            totalInterest = totalInterest.add(tier2Amount.multiply(tier2Rate));
            remainingBalance = remainingBalance.subtract(tier2Amount);
        }
        
        // Tier 3: Above 100,000 at 8% p.a
        if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier3Rate = new BigDecimal("0.08").divide(new BigDecimal("365"), 10, BigDecimal.ROUND_HALF_UP);
            totalInterest = totalInterest.add(remainingBalance.multiply(tier3Rate));
        }
        
        return totalInterest;
    }
}
