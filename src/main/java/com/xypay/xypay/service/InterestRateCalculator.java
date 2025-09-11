package com.xypay.xypay.service;


import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InterestRateCalculator {
    
    // Tier thresholds in NGN
    public static final BigDecimal TIER_1_THRESHOLD = new BigDecimal("10000");  // 10,000 NGN
    public static final BigDecimal TIER_2_THRESHOLD = new BigDecimal("100000"); // 100,000 NGN
    
    // Interest rates per annum (as decimals)
    public static final BigDecimal TIER_1_RATE = new BigDecimal("0.20");  // 20% p.a.
    public static final BigDecimal TIER_2_RATE = new BigDecimal("0.16");  // 16% p.a.
    public static final BigDecimal TIER_3_RATE = new BigDecimal("0.08");  // 8% p.a.
    
    /**
     * Calculate interest for a given balance over specified days.
     *
     * @param balance The balance amount
     * @param days Number of days to calculate interest for (default: 365 for annual)
     * @return Interest amount calculated
     */
    public BigDecimal calculateInterestForBalance(BigDecimal balance, int days) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Calculate daily rate (annual rate / 365)
        BigDecimal dailyTier1Rate = TIER_1_RATE.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
        BigDecimal dailyTier2Rate = TIER_2_RATE.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
        BigDecimal dailyTier3Rate = TIER_3_RATE.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
        
        // Calculate interest for each tier
        BigDecimal interest = BigDecimal.ZERO;
        
        if (balance.compareTo(TIER_1_THRESHOLD) <= 0) {
            // All balance in Tier 1 (20% p.a.)
            interest = balance.multiply(dailyTier1Rate).multiply(new BigDecimal(days));
        } else if (balance.compareTo(TIER_2_THRESHOLD) <= 0) {
            // First 10,000 at 20%, remaining at 16%
            BigDecimal tier1Interest = TIER_1_THRESHOLD.multiply(dailyTier1Rate).multiply(new BigDecimal(days));
            BigDecimal tier2Balance = balance.subtract(TIER_1_THRESHOLD);
            BigDecimal tier2Interest = tier2Balance.multiply(dailyTier2Rate).multiply(new BigDecimal(days));
            interest = tier1Interest.add(tier2Interest);
        } else {
            // First 10,000 at 20%, 10,000-100,000 at 16%, remaining at 8%
            BigDecimal tier1Interest = TIER_1_THRESHOLD.multiply(dailyTier1Rate).multiply(new BigDecimal(days));
            BigDecimal tier2Balance = TIER_2_THRESHOLD.subtract(TIER_1_THRESHOLD);
            BigDecimal tier2Interest = tier2Balance.multiply(dailyTier2Rate).multiply(new BigDecimal(days));
            BigDecimal tier3Balance = balance.subtract(TIER_2_THRESHOLD);
            BigDecimal tier3Interest = tier3Balance.multiply(dailyTier3Rate).multiply(new BigDecimal(days));
            interest = tier1Interest.add(tier2Interest).add(tier3Interest);
        }
        
        return interest.setScale(4, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate interest with detailed breakdown by tier.
     *
     * @param balance The balance amount
     * @param days Number of days to calculate interest for
     * @return Detailed breakdown of interest calculation
     */
    public Map<String, Object> calculateInterestBreakdown(BigDecimal balance, int days) {
        Map<String, Object> result = new HashMap<>();
        
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
            result.put("totalInterest", BigDecimal.ZERO);
            result.put("breakdown", new ArrayList<>());
            result.put("effectiveRate", BigDecimal.ZERO);
            return result;
        }
        
        BigDecimal daysDecimal = new BigDecimal(days);
        
        // Calculate daily rates
        BigDecimal dailyTier1Rate = TIER_1_RATE.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
        BigDecimal dailyTier2Rate = TIER_2_RATE.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
        BigDecimal dailyTier3Rate = TIER_3_RATE.divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
        
        List<Map<String, Object>> breakdown = new ArrayList<>();
        BigDecimal totalInterest = BigDecimal.ZERO;
        
        if (balance.compareTo(TIER_1_THRESHOLD) <= 0) {
            // All in Tier 1
            BigDecimal tier1Interest = balance.multiply(dailyTier1Rate).multiply(daysDecimal);
            Map<String, Object> tier1 = new HashMap<>();
            tier1.put("tier", 1);
            tier1.put("rate", TIER_1_RATE);
            tier1.put("balanceInTier", balance);
            tier1.put("interest", tier1Interest);
            tier1.put("description", "Balance up to " + TIER_1_THRESHOLD + " at " + TIER_1_RATE.multiply(new BigDecimal("100")) + "% p.a.");
            breakdown.add(tier1);
            totalInterest = tier1Interest;
            
        } else if (balance.compareTo(TIER_2_THRESHOLD) <= 0) {
            // Tier 1 + Tier 2
            BigDecimal tier1Interest = TIER_1_THRESHOLD.multiply(dailyTier1Rate).multiply(daysDecimal);
            BigDecimal tier2Balance = balance.subtract(TIER_1_THRESHOLD);
            BigDecimal tier2Interest = tier2Balance.multiply(dailyTier2Rate).multiply(daysDecimal);
            
            Map<String, Object> tier1 = new HashMap<>();
            tier1.put("tier", 1);
            tier1.put("rate", TIER_1_RATE);
            tier1.put("balanceInTier", TIER_1_THRESHOLD);
            tier1.put("interest", tier1Interest);
            tier1.put("description", "First " + TIER_1_THRESHOLD + " at " + TIER_1_RATE.multiply(new BigDecimal("100")) + "% p.a.");
            breakdown.add(tier1);
            
            Map<String, Object> tier2 = new HashMap<>();
            tier2.put("tier", 2);
            tier2.put("rate", TIER_2_RATE);
            tier2.put("balanceInTier", tier2Balance);
            tier2.put("interest", tier2Interest);
            tier2.put("description", "Balance " + TIER_1_THRESHOLD + " - " + TIER_2_THRESHOLD + " at " + TIER_2_RATE.multiply(new BigDecimal("100")) + "% p.a.");
            breakdown.add(tier2);
            
            totalInterest = tier1Interest.add(tier2Interest);
            
        } else {
            // All three tiers
            BigDecimal tier1Interest = TIER_1_THRESHOLD.multiply(dailyTier1Rate).multiply(daysDecimal);
            BigDecimal tier2Balance = TIER_2_THRESHOLD.subtract(TIER_1_THRESHOLD);
            BigDecimal tier2Interest = tier2Balance.multiply(dailyTier2Rate).multiply(daysDecimal);
            BigDecimal tier3Balance = balance.subtract(TIER_2_THRESHOLD);
            BigDecimal tier3Interest = tier3Balance.multiply(dailyTier3Rate).multiply(daysDecimal);
            
            Map<String, Object> tier1 = new HashMap<>();
            tier1.put("tier", 1);
            tier1.put("rate", TIER_1_RATE);
            tier1.put("balanceInTier", TIER_1_THRESHOLD);
            tier1.put("interest", tier1Interest);
            tier1.put("description", "First " + TIER_1_THRESHOLD + " at " + TIER_1_RATE.multiply(new BigDecimal("100")) + "% p.a.");
            breakdown.add(tier1);
            
            Map<String, Object> tier2 = new HashMap<>();
            tier2.put("tier", 2);
            tier2.put("rate", TIER_2_RATE);
            tier2.put("balanceInTier", tier2Balance);
            tier2.put("interest", tier2Interest);
            tier2.put("description", "Balance " + TIER_1_THRESHOLD + " - " + TIER_2_THRESHOLD + " at " + TIER_2_RATE.multiply(new BigDecimal("100")) + "% p.a.");
            breakdown.add(tier2);
            
            Map<String, Object> tier3 = new HashMap<>();
            tier3.put("tier", 3);
            tier3.put("rate", TIER_3_RATE);
            tier3.put("balanceInTier", tier3Balance);
            tier3.put("interest", tier3Interest);
            tier3.put("description", "Balance above " + TIER_2_THRESHOLD + " at " + TIER_3_RATE.multiply(new BigDecimal("100")) + "% p.a.");
            breakdown.add(tier3);
            
            totalInterest = tier1Interest.add(tier2Interest).add(tier3Interest);
        }
        
        // Calculate effective annual rate
        BigDecimal effectiveRate = BigDecimal.ZERO;
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            effectiveRate = totalInterest.divide(balance, 10, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("365")).divide(daysDecimal, 10, RoundingMode.HALF_UP);
        }
        
        result.put("totalInterest", totalInterest.setScale(4, RoundingMode.HALF_UP));
        result.put("breakdown", breakdown);
        result.put("effectiveRate", effectiveRate);
        result.put("calculationPeriodDays", days);
        
        return result;
    }
    
    /**
     * Calculate interest for a specific month.
     *
     * @param balance The balance amount
     * @param month Month number (1-12), if null uses current month
     * @param year Year, if null uses current year
     * @return Monthly interest amount
     */
    public BigDecimal calculateMonthlyInterest(BigDecimal balance, Integer month, Integer year) {
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        // Get number of days in the month
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        
        return calculateInterestForBalance(balance, daysInMonth);
    }
    
    /**
     * Calculate annual interest for a balance.
     *
     * @param balance The balance amount
     * @return Annual interest amount
     */
    public BigDecimal calculateAnnualInterest(BigDecimal balance) {
        return calculateInterestForBalance(balance, 365);
    }
}