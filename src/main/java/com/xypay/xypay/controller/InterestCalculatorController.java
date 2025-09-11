package com.xypay.xypay.controller;

import com.xypay.xypay.dto.ApiResponse;
import com.xypay.xypay.dto.InterestCalculationRequest;
import com.xypay.xypay.dto.InterestCalculationResponse;
import com.xypay.xypay.service.InterestRateCalculator;
import com.xypay.xypay.service.InterestReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/interest")
public class InterestCalculatorController {
    
    private static final Logger logger = LoggerFactory.getLogger(InterestCalculatorController.class);
    
    @Autowired
    private InterestRateCalculator interestRateCalculator;
    
    @Autowired
    private InterestReportService interestReportService;
    
    /**
     * Calculate interest for a given balance amount.
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<InterestCalculationResponse>> calculateInterest(
            @RequestBody InterestCalculationRequest request) {
        try {
            BigDecimal balanceAmount = request.getBalance();
            Integer days = request.getDays() != null ? request.getDays() : 365;
            String currency = request.getCurrency() != null ? request.getCurrency() : "NGN";
            
            if (balanceAmount == null || balanceAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Balance must be greater than 0", null));
            }
            
            if (days <= 0) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Days must be greater than 0", null));
            }
            
            // Calculate interest with breakdown
            Map<String, Object> result = interestRateCalculator.calculateInterestBreakdown(balanceAmount, days);
            
            InterestCalculationResponse response = new InterestCalculationResponse();
            response.setBalance(balanceAmount);
            response.setCalculationPeriodDays(days);
            response.setTotalInterest((BigDecimal) result.get("totalInterest"));
            response.setEffectiveRate(((BigDecimal) result.get("effectiveRate")).multiply(new BigDecimal("100")));
            response.setBreakdown((List<Map<String, Object>>) result.get("breakdown"));
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Interest calculated successfully", response));
            
        } catch (Exception e) {
            logger.error("Error calculating interest: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Failed to calculate interest: " + e.getMessage(), null));
        }
    }
    
    /**
     * Get current interest rate information.
     */
    @GetMapping("/rates")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInterestRates() {
        try {
            Map<String, Object> ratesInfo = interestReportService.getInterestRatesInfo();
            return ResponseEntity.ok(new ApiResponse<>(true, "Interest rates retrieved successfully", ratesInfo));
            
        } catch (Exception e) {
            logger.error("Error getting interest rates: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Failed to get interest rates: " + e.getMessage(), null));
        }
    }
    
    /**
     * Demo endpoint showing interest calculations for different balance amounts.
     */
    @GetMapping("/demo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> interestCalculatorDemo() {
        try {
            BigDecimal[] demoBalances = {
                new BigDecimal("5000"),    // Tier 1 only
                new BigDecimal("25000"),   // Tier 1 + Tier 2
                new BigDecimal("150000"),  // All three tiers
                new BigDecimal("500000")   // All three tiers
            };
            
            Map<String, Object> demoResults = new HashMap<>();
            
            for (int i = 0; i < demoBalances.length; i++) {
                BigDecimal balance = demoBalances[i];
                // Calculate annual interest
                Map<String, Object> annualResult = interestRateCalculator.calculateInterestBreakdown(balance, 365);
                
                // Calculate monthly interest
                Map<String, Object> monthlyResult = interestRateCalculator.calculateInterestBreakdown(balance, 30);
                
                Map<String, Object> balanceResult = new HashMap<>();
                balanceResult.put("balance", balance);
                balanceResult.put("annualInterest", annualResult.get("totalInterest"));
                balanceResult.put("monthlyInterest", monthlyResult.get("totalInterest"));
                balanceResult.put("effectiveAnnualRate", 
                    ((BigDecimal) annualResult.get("effectiveRate")).multiply(new BigDecimal("100")));
                balanceResult.put("breakdown", annualResult.get("breakdown"));
                
                demoResults.put("balance_" + i, balanceResult);
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("demoCalculations", demoResults);
            responseData.put("rateStructure", interestReportService.getInterestRatesInfo());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Demo calculations generated successfully", responseData));
            
        } catch (Exception e) {
            logger.error("Error in interest calculator demo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(false, "Failed to generate demo calculations: " + e.getMessage(), null));
        }
    }
}