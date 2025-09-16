package com.xypay.xypay.service;

import com.xypay.xypay.domain.XySaveAccount;
import com.xypay.xypay.domain.XySaveSettings;
import com.xypay.xypay.domain.XySaveTransaction;
import com.xypay.xypay.domain.XySaveGoal;
import com.xypay.xypay.domain.XySaveInvestment;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.XySaveAccountRepository;
import com.xypay.xypay.repository.XySaveSettingsRepository;
import com.xypay.xypay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class XySaveAccountService {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveAccountService.class);
    
    @Autowired
    private XySaveAccountRepository xySaveAccountRepository;
    
    @Autowired
    private XySaveSettingsRepository xySaveSettingsRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create a new XySave account for user
     */
    @Transactional
    public XySaveAccount createXySaveAccount(User user) {
        try {
            // Generate unique account number
            String accountNumber = generateAccountNumber(user);
            
            // Create XySave account
            XySaveAccount xySaveAccount = new XySaveAccount();
            xySaveAccount.setUser(user);
            xySaveAccount.setAccountNumber(accountNumber);
            xySaveAccount.setDailyInterestRate(new BigDecimal("0.0004")); // 0.04% daily = ~15% annual
            xySaveAccount.setBalance(BigDecimal.ZERO);
            xySaveAccount.setTotalInterestEarned(BigDecimal.ZERO);
            xySaveAccount.setIsActive(true);
            xySaveAccount.setAutoSaveEnabled(false);
            xySaveAccount.setAutoSavePercentage(new BigDecimal("10.00"));
            xySaveAccount.setAutoSaveMinAmount(new BigDecimal("100.00"));
            xySaveAccount.setLastInterestCalculation(LocalDateTime.now());
            
            XySaveAccount savedAccount = xySaveAccountRepository.save(xySaveAccount);
            
            // Create default settings
            XySaveSettings settings = new XySaveSettings();
            settings.setUser(user);
            xySaveSettingsRepository.save(settings);
            
            logger.info("Created XySave account {} for user {}", accountNumber, user.getUsername());
            return savedAccount;
            
        } catch (Exception e) {
            logger.error("Error creating XySave account for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get or create XySave account for user
     */
    @Transactional
    public XySaveAccount getXySaveAccount(User user) {
        try {
            Optional<XySaveAccount> existingAccount = xySaveAccountRepository.findByUser(user);
            if (existingAccount.isPresent()) {
                return existingAccount.get();
            }
            return createXySaveAccount(user);
        } catch (Exception e) {
            logger.error("Error getting XySave account for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get comprehensive account summary
     */
    public Map<String, Object> getAccountSummary(User user) {
        try {
            XySaveAccount account = getXySaveAccount(user);
            XySaveSettings settings = xySaveSettingsRepository.findByUser(user)
                .orElseGet(() -> {
                    XySaveSettings newSettings = new XySaveSettings();
                    newSettings.setUser(user);
                    return xySaveSettingsRepository.save(newSettings);
                });
            
            // Calculate today's interest
            BigDecimal dailyInterest = account.calculateDailyInterest();
            
            // Get recent transactions (last 5)
            List<XySaveTransaction> recentTransactions = account.getTransactions() != null ? 
                account.getTransactions().stream()
                    .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                    .limit(5)
                    .toList() : List.of();
            
            // Get active goals
            List<XySaveGoal> activeGoals = account.getGoals() != null ? 
                account.getGoals().stream()
                    .filter(goal -> Boolean.TRUE.equals(goal.getIsActive()))
                    .toList() : List.of();
            
            // Get investments
            List<XySaveInvestment> investments = account.getInvestments() != null ? 
                account.getInvestments().stream()
                    .filter(inv -> Boolean.TRUE.equals(inv.getIsActive()))
                    .toList() : List.of();
            
            // Calculate totals
            BigDecimal totalInvested = investments.stream()
                .map(XySaveInvestment::getAmountInvested)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalInvestmentValue = investments.stream()
                .map(XySaveInvestment::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("account", account);
            summary.put("settings", settings);
            summary.put("daily_interest", dailyInterest);
            summary.put("annual_interest_rate", account.getAnnualInterestRate());
            summary.put("recent_transactions", recentTransactions);
            summary.put("active_goals", activeGoals);
            summary.put("investments", investments);
            summary.put("total_invested", totalInvested);
            summary.put("total_investment_value", totalInvestmentValue);
            
            return summary;
            
        } catch (Exception e) {
            logger.error("Error getting XySave summary for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Generate unique account number
     */
    private String generateAccountNumber(User user) {
        String userId = String.format("%08d", user.getId());
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        return "XS" + userId + timestamp;
    }
}
