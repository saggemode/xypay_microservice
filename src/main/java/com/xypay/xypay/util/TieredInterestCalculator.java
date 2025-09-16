package com.xypay.xypay.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class TieredInterestCalculator {
    
    private static final int TIER_1_THRESHOLD = 10000;
    private static final int TIER_2_THRESHOLD = 100000;
    private static final BigDecimal TIER_1_RATE = new BigDecimal("0.20"); // 20% p.a
    private static final BigDecimal TIER_2_RATE = new BigDecimal("0.16"); // 16% p.a
    private static final BigDecimal TIER_3_RATE = new BigDecimal("0.08"); // 8% p.a
    
    // Daily rates
    private static final BigDecimal DAILY_TIER_1_RATE = TIER_1_RATE.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
    private static final BigDecimal DAILY_TIER_2_RATE = TIER_2_RATE.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
    private static final BigDecimal DAILY_TIER_3_RATE = TIER_3_RATE.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
    
    /**
     * Calculate tiered interest rate for a given balance amount
     * Returns: Map with tier breakdown and total interest
     */
    public static Map<String, Object> calculateTieredInterestRate(BigDecimal balanceAmount) {
        Map<String, Object> result = new HashMap<>();
        
        if (balanceAmount == null || balanceAmount.compareTo(BigDecimal.ZERO) <= 0) {
            result.put("tier_1", createTierData(BigDecimal.ZERO, 20, DAILY_TIER_1_RATE, BigDecimal.ZERO));
            result.put("tier_2", createTierData(BigDecimal.ZERO, 16, DAILY_TIER_2_RATE, BigDecimal.ZERO));
            result.put("tier_3", createTierData(BigDecimal.ZERO, 8, DAILY_TIER_3_RATE, BigDecimal.ZERO));
            result.put("total_interest", BigDecimal.ZERO);
            return result;
        }
        
        BigDecimal remainingBalance = balanceAmount;
        BigDecimal totalInterest = BigDecimal.ZERO;
        
        // Tier 1: First 10,000 at 20% p.a
        BigDecimal tier1Amount = remainingBalance.min(new BigDecimal(TIER_1_THRESHOLD));
        BigDecimal tier1Interest = tier1Amount.multiply(DAILY_TIER_1_RATE);
        totalInterest = totalInterest.add(tier1Interest);
        remainingBalance = remainingBalance.subtract(tier1Amount);
        
        result.put("tier_1", createTierData(tier1Amount, 20, DAILY_TIER_1_RATE, tier1Interest));
        
        // Tier 2: 10,001 - 100,000 at 16% p.a
        BigDecimal tier2Amount = remainingBalance.min(new BigDecimal(TIER_2_THRESHOLD - TIER_1_THRESHOLD));
        BigDecimal tier2Interest = tier2Amount.multiply(DAILY_TIER_2_RATE);
        totalInterest = totalInterest.add(tier2Interest);
        remainingBalance = remainingBalance.subtract(tier2Amount);
        
        result.put("tier_2", createTierData(tier2Amount, 16, DAILY_TIER_2_RATE, tier2Interest));
        
        // Tier 3: Above 100,000 at 8% p.a
        BigDecimal tier3Amount = remainingBalance;
        BigDecimal tier3Interest = tier3Amount.multiply(DAILY_TIER_3_RATE);
        totalInterest = totalInterest.add(tier3Interest);
        
        result.put("tier_3", createTierData(tier3Amount, 8, DAILY_TIER_3_RATE, tier3Interest));
        result.put("total_interest", totalInterest);
        
        return result;
    }
    
    private static Map<String, Object> createTierData(BigDecimal amount, int rate, BigDecimal dailyRate, BigDecimal interest) {
        Map<String, Object> tierData = new HashMap<>();
        tierData.put("amount", amount != null ? amount.doubleValue() : 0.0);
        tierData.put("rate", rate);
        tierData.put("daily_rate", dailyRate != null ? dailyRate.doubleValue() : 0.0);
        tierData.put("interest", interest != null ? interest.doubleValue() : 0.0);
        return tierData;
    }
    
    /**
     * Calculate daily interest for a given balance
     */
    public static BigDecimal calculateDailyInterest(BigDecimal balance) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        Map<String, Object> breakdown = calculateTieredInterestRate(balance);
        return (BigDecimal) breakdown.get("total_interest");
    }
    
    /**
     * Get effective annual percentage rate based on current balance and tiers
     */
    public static BigDecimal getEffectiveAnnualRate(BigDecimal balance) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal dailyInterest = calculateDailyInterest(balance);
        BigDecimal effectiveDailyRate = dailyInterest.divide(balance, 10, RoundingMode.HALF_UP);
        return effectiveDailyRate.multiply(new BigDecimal("36500")); // 365 * 100
    }
}
