package com.xypay.xypay.service;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class InterestAccrualService {
    
    private static final Logger logger = LoggerFactory.getLogger(InterestAccrualService.class);
    
    @Autowired
    private InterestRateCalculator interestRateCalculator;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    /**
     * Calculate interest for a wallet over a specific period.
     *
     * @param wallet The wallet to calculate interest for
     * @param fromDate Start date for calculation (default: 30 days ago)
     * @param toDate End date for calculation (default: now)
     * @return Interest calculation results
     */
    public Map<String, Object> calculateWalletInterest(Wallet wallet, LocalDateTime fromDate, LocalDateTime toDate) {
        if (fromDate == null) {
            fromDate = LocalDateTime.now().minusDays(30);
        }
        if (toDate == null) {
            toDate = LocalDateTime.now();
        }
        
        // In a real implementation, you would get transactions for the period
        // For now, we'll use the current balance and the time period
        int days = (int) java.time.Duration.between(fromDate, toDate).toDays();
        return interestRateCalculator.calculateInterestBreakdown(wallet.getBalance(), days);
    }
    
    /**
     * Apply interest to a wallet by creating a credit transaction.
     *
     * @param wallet The wallet to credit
     * @param interestAmount Amount of interest to apply
     * @param description Description for the transaction
     * @return The created interest transaction
     */
    public Transaction applyInterestToWallet(Wallet wallet, BigDecimal interestAmount, String description) {
        if (interestAmount == null || interestAmount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Attempted to apply zero or negative interest to wallet {}", wallet.getId());
            return null;
        }
        
        try {
            // Create interest transaction
            Transaction transaction = new Transaction();
            transaction.setWallet(wallet);
            transaction.setAmount(interestAmount);
            transaction.setType("credit");
            transaction.setChannel("interest");
            transaction.setDescription(description);
            transaction.setStatus("success");
            transaction.setBalanceAfter(wallet.getBalance().add(interestAmount));
            transaction.setTimestamp(LocalDateTime.now().withNano(0)); // Avoid precision issues with DB timestamp
            
            transaction = transactionRepository.save(transaction);
            
            // Note: In a real implementation, you would update the wallet balance here
            // For now, we're just creating the transaction record
            
            logger.info("Applied {} interest to wallet {}", interestAmount, wallet.getId());
            return transaction;
            
        } catch (Exception e) {
            logger.error("Failed to apply interest to wallet {}: {}", wallet.getId(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Process monthly interest for a wallet.
     *
     * @param wallet The wallet to process interest for
     * @return The created interest transaction
     */
    public Transaction processMonthlyInterest(Wallet wallet) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate lastMonth = now.minusMonths(1).toLocalDate().withDayOfMonth(1);
        LocalDateTime lastMonthStart = lastMonth.atStartOfDay();
        
        Map<String, Object> interestCalculation = calculateWalletInterest(wallet, lastMonthStart, now);
        BigDecimal interestAmount = (BigDecimal) interestCalculation.get("totalInterest");
        
        if (interestAmount != null && interestAmount.compareTo(BigDecimal.ZERO) > 0) {
            String description = String.format("Monthly interest for %s %d", 
                    lastMonth.getMonth().name(), lastMonth.getYear());
            return applyInterestToWallet(wallet, interestAmount, description);
        }
        
        return null;
    }
}