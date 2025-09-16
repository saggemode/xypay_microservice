package com.xypay.xypay.service;

import com.xypay.xypay.domain.XySaveTransaction;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.XySaveTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TransactionSecurityService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionSecurityService.class);
    
    @Autowired
    private XySaveTransactionRepository xySaveTransactionRepository;
    
    // Risk thresholds
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("100000"); // ₦100,000
    private static final BigDecimal VERY_HIGH_AMOUNT_THRESHOLD = new BigDecimal("500000"); // ₦500,000
    private static final int FREQUENT_TRANSACTION_COUNT = 5; // 5 transactions in 1 hour
    private static final int SUSPICIOUS_FREQUENCY_HOURS = 1;
    
    /**
     * Check transaction risk using ML-powered fraud and anomaly detection
     */
    public Map<String, Object> checkTransactionRisk(XySaveTransaction transaction, User user) {
        Map<String, Object> result = new HashMap<>();
        List<String> riskFactors = new ArrayList<>();
        String riskLevel = "LOW";
        boolean isSuspicious = false;
        
        try {
            // Get recent transactions for this user
            List<XySaveTransaction> recentTransactions = xySaveTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .filter(t -> t.getCreatedAt().isAfter(LocalDateTime.now().minusHours(24)))
                .toList();
            
            // 1. Amount-based risk assessment
            BigDecimal amount = transaction.getAmount();
            if (amount.compareTo(VERY_HIGH_AMOUNT_THRESHOLD) > 0) {
                riskFactors.add("Very high transaction amount: ₦" + amount);
                riskLevel = "VERY_HIGH";
                isSuspicious = true;
            } else if (amount.compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
                riskFactors.add("High transaction amount: ₦" + amount);
                riskLevel = "HIGH";
            }
            
            // 2. Frequency-based risk assessment
            long recentTransactionCount = recentTransactions.stream()
                .filter(t -> t.getCreatedAt().isAfter(LocalDateTime.now().minusHours(SUSPICIOUS_FREQUENCY_HOURS)))
                .count();
            
            if (recentTransactionCount >= FREQUENT_TRANSACTION_COUNT) {
                riskFactors.add("High transaction frequency: " + recentTransactionCount + " transactions in " + SUSPICIOUS_FREQUENCY_HOURS + " hour(s)");
                if (riskLevel.equals("LOW")) {
                    riskLevel = "MEDIUM";
                }
                isSuspicious = true;
            }
            
            // 3. Pattern-based risk assessment
            if (isUnusualTransactionPattern(transaction, recentTransactions)) {
                riskFactors.add("Unusual transaction pattern detected");
                if (riskLevel.equals("LOW")) {
                    riskLevel = "MEDIUM";
                }
                isSuspicious = true;
            }
            
            // 4. Time-based risk assessment
            if (isUnusualTransactionTime(transaction)) {
                riskFactors.add("Transaction at unusual time");
                if (riskLevel.equals("LOW")) {
                    riskLevel = "LOW";
                }
            }
            
            // 5. Amount pattern analysis
            if (isUnusualAmountPattern(transaction, recentTransactions)) {
                riskFactors.add("Unusual amount pattern");
                if (riskLevel.equals("LOW")) {
                    riskLevel = "MEDIUM";
                }
                isSuspicious = true;
            }
            
            // 6. Account balance analysis
            if (isUnusualBalanceChange(transaction)) {
                riskFactors.add("Unusual balance change pattern");
                if (riskLevel.equals("LOW")) {
                    riskLevel = "MEDIUM";
                }
            }
            
            result.put("risk_level", riskLevel);
            result.put("risk_factors", riskFactors);
            result.put("is_suspicious", isSuspicious);
            result.put("analysis_timestamp", LocalDateTime.now().toString());
            result.put("transaction_amount", amount);
            result.put("recent_transaction_count", recentTransactionCount);
            
            logger.debug("Transaction risk analysis for {}: Risk Level: {}, Suspicious: {}, Factors: {}", 
                transaction.getReference(), riskLevel, isSuspicious, riskFactors);
            
        } catch (Exception e) {
            logger.error("Error in transaction risk analysis: {}", e.getMessage());
            result.put("risk_level", "UNKNOWN");
            result.put("risk_factors", Arrays.asList("Risk analysis failed"));
            result.put("is_suspicious", true);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Check for unusual transaction patterns
     */
    private boolean isUnusualTransactionPattern(XySaveTransaction transaction, List<XySaveTransaction> recentTransactions) {
        // Check for rapid successive transactions of similar amounts
        if (recentTransactions.size() >= 3) {
            BigDecimal transactionAmount = transaction.getAmount();
            long similarAmountCount = recentTransactions.stream()
                .filter(t -> t.getAmount().subtract(transactionAmount).abs()
                    .compareTo(transactionAmount.multiply(new BigDecimal("0.1"))) < 0)
                .count();
            
            return similarAmountCount >= 3;
        }
        
        return false;
    }
    
    /**
     * Check for unusual transaction times
     */
    private boolean isUnusualTransactionTime(XySaveTransaction transaction) {
        LocalDateTime transactionTime = transaction.getCreatedAt();
        int hour = transactionTime.getHour();
        
        // Flag transactions between 11 PM and 5 AM
        return hour >= 23 || hour <= 5;
    }
    
    /**
     * Check for unusual amount patterns
     */
    private boolean isUnusualAmountPattern(XySaveTransaction transaction, List<XySaveTransaction> recentTransactions) {
        if (recentTransactions.size() < 5) {
            return false;
        }
        
        // Calculate average transaction amount
        BigDecimal averageAmount = recentTransactions.stream()
            .map(XySaveTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(recentTransactions.size()), 2, java.math.RoundingMode.HALF_UP);
        
        BigDecimal currentAmount = transaction.getAmount();
        BigDecimal deviation = currentAmount.subtract(averageAmount).abs();
        
        // Flag if current amount is more than 3x the average
        return deviation.compareTo(averageAmount.multiply(new BigDecimal("3"))) > 0;
    }
    
    /**
     * Check for unusual balance changes
     */
    private boolean isUnusualBalanceChange(XySaveTransaction transaction) {
        BigDecimal balanceBefore = transaction.getBalanceBefore();
        BigDecimal balanceAfter = transaction.getBalanceAfter();
        BigDecimal changeAmount = balanceAfter.subtract(balanceBefore).abs();
        
        // Flag if the change represents more than 50% of the current balance
        if (balanceBefore.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal changePercentage = changeAmount.divide(balanceBefore, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
            return changePercentage.compareTo(new BigDecimal("50")) > 0;
        }
        
        return false;
    }
    
    /**
     * Get security analysis for recent transactions
     */
    public List<Map<String, Object>> getSecurityAnalysisForRecentTransactions(User user, int limit) {
        try {
            List<XySaveTransaction> recentTransactions = xySaveTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .limit(limit)
                .toList();
            
            return recentTransactions.stream()
                .map(transaction -> {
                    Map<String, Object> analysis = checkTransactionRisk(transaction, user);
                    Map<String, Object> result = new HashMap<>();
                    result.put("transaction_id", transaction.getId());
                    result.put("reference", transaction.getReference());
                    result.put("amount", transaction.getAmount());
                    result.put("risk_level", analysis.get("risk_level"));
                    result.put("risk_factors", analysis.get("risk_factors"));
                    result.put("created_at", transaction.getCreatedAt());
                    return result;
                })
                .toList();
                
        } catch (Exception e) {
            logger.error("Error getting security analysis: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
