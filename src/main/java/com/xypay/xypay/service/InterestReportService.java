package com.xypay.xypay.service;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InterestReportService {
    
    @Autowired
    private InterestRateCalculator interestRateCalculator;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    /**
     * Generate a comprehensive interest report for a wallet.
     *
     * @param wallet The wallet to generate report for
     * @param startDate Report start date
     * @param endDate Report end date
     * @return Comprehensive interest report
     */
    public Map<String, Object> generateInterestReport(Wallet wallet, LocalDateTime startDate, LocalDateTime endDate) {
        // Get all interest transactions in the period
        List<Transaction> interestTransactions = transactionRepository.findByWalletAndChannelAndTimestampBetweenAndStatus(
                wallet, "interest", startDate, endDate, "success");
        
        BigDecimal totalInterestPaid = BigDecimal.ZERO;
        for (Transaction t : interestTransactions) {
            totalInterestPaid = totalInterestPaid.add(t.getAmount());
        }
        
        // Calculate what interest should have been paid
        int days = (int) java.time.Duration.between(startDate, endDate).toDays();
        Map<String, Object> expectedInterest = interestRateCalculator.calculateInterestBreakdown(
                wallet.getBalance(), days);
        
        Map<String, Object> result = new HashMap<>();
        result.put("walletId", wallet.getId());
        result.put("accountNumber", wallet.getAccountNumber());
        
        Map<String, Object> reportPeriod = new HashMap<>();
        reportPeriod.put("startDate", startDate);
        reportPeriod.put("endDate", endDate);
        reportPeriod.put("days", days);
        result.put("reportPeriod", reportPeriod);
        
        Map<String, Object> interestSummary = new HashMap<>();
        BigDecimal expectedInterestAmount = (BigDecimal) expectedInterest.get("totalInterest");
        interestSummary.put("totalInterestPaid", totalInterestPaid);
        interestSummary.put("expectedInterest", expectedInterestAmount);
        interestSummary.put("difference", totalInterestPaid.subtract(expectedInterestAmount));
        interestSummary.put("effectiveRate", expectedInterest.get("effectiveRate"));
        result.put("interestSummary", interestSummary);
        
        result.put("interestBreakdown", expectedInterest.get("breakdown"));
        
        List<Map<String, Object>> transactions = new ArrayList<>();
        for (Transaction t : interestTransactions) {
            Map<String, Object> transaction = new HashMap<>();
            transaction.put("id", t.getId());
            transaction.put("amount", t.getAmount());
            transaction.put("timestamp", t.getTimestamp());
            transaction.put("description", t.getDescription());
            transactions.add(transaction);
        }
        result.put("transactions", transactions);
        
        return result;
    }
    
    /**
     * Get information about current interest rates.
     *
     * @return Current interest rate information
     */
    public Map<String, Object> getInterestRatesInfo() {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> tier1 = new HashMap<>();
        tier1.put("threshold", InterestRateCalculator.TIER_1_THRESHOLD);
        tier1.put("rate", InterestRateCalculator.TIER_1_RATE.multiply(new BigDecimal("100")) + "% p.a.");
        tier1.put("description", "Balance up to " + InterestRateCalculator.TIER_1_THRESHOLD);
        result.put("tier1", tier1);
        
        Map<String, Object> tier2 = new HashMap<>();
        tier2.put("threshold", InterestRateCalculator.TIER_2_THRESHOLD);
        tier2.put("rate", InterestRateCalculator.TIER_2_RATE.multiply(new BigDecimal("100")) + "% p.a.");
        tier2.put("description", "Balance " + InterestRateCalculator.TIER_1_THRESHOLD + " - " + InterestRateCalculator.TIER_2_THRESHOLD);
        result.put("tier2", tier2);
        
        Map<String, Object> tier3 = new HashMap<>();
        tier3.put("rate", InterestRateCalculator.TIER_3_RATE.multiply(new BigDecimal("100")) + "% p.a.");
        tier3.put("description", "Balance above " + InterestRateCalculator.TIER_2_THRESHOLD);
        result.put("tier3", tier3);
        
        return result;
    }
}