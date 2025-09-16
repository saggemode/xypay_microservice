package com.xypay.xypay.controller;

import com.xypay.xypay.domain.SpendAndSaveAccount;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.dto.ApiResponse;
import com.xypay.xypay.dto.SpendAndSaveTransactionDTO;
import com.xypay.xypay.service.SpendAndSaveService;
import com.xypay.xypay.service.SpendAndSaveInterestService;
import com.xypay.xypay.repository.SpendAndSaveAccountRepository;
import com.xypay.xypay.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for interest-related operations
 */
@RestController
@RequestMapping("/api/spend-and-save/interest")
@Slf4j
public class SpendAndSaveInterestController {
    
    @Autowired
    private SpendAndSaveService spendAndSaveService;
    
    @Autowired
    private SpendAndSaveInterestService interestService;
    
    @Autowired
    private SpendAndSaveAccountRepository spendAndSaveAccountRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Calculate interest for a given balance amount
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculateInterest(
            @Valid @RequestBody InterestCalculationRequestDTO request,
            Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Optional<SpendAndSaveAccount> accountOpt = spendAndSaveAccountRepository.findByUser(user);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("Spend and Save account not found"));
            }
            
            SpendAndSaveAccount account = accountOpt.get();
            
            // Calculate tiered interest for the given balance
            BigDecimal balance = request.getBalanceAmount();
            BigDecimal dailyInterest = calculateTieredInterest(balance);
            BigDecimal monthlyInterest = dailyInterest.multiply(new BigDecimal("30"));
            BigDecimal annualInterest = dailyInterest.multiply(new BigDecimal("365"));
            
            Map<String, Object> result = Map.of(
                "balance_amount", balance,
                "daily_interest", dailyInterest,
                "monthly_interest", monthlyInterest,
                "annual_interest", annualInterest,
                "tier_breakdown", getTierBreakdown(balance)
            );
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("Error calculating interest: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error calculating interest: " + e.getMessage()));
        }
    }
    
    /**
     * Credit daily interest to user's account
     */
    @PostMapping("/credit")
    public ResponseEntity<ApiResponse<SpendAndSaveTransactionDTO>> creditInterest(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            var interestTx = spendAndSaveService.calculateAndCreditInterest(user);
            
            if (interestTx == null) {
                return ResponseEntity.ok(ApiResponse.success(null, "No interest to credit (zero balance)"));
            }
            
            SpendAndSaveTransactionDTO transactionDTO = new SpendAndSaveTransactionDTO(interestTx);
            return ResponseEntity.ok(ApiResponse.success(transactionDTO, "Interest credited successfully"));
            
        } catch (Exception e) {
            log.error("Error crediting interest: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error crediting interest: " + e.getMessage()));
        }
    }
    
    /**
     * Get information about tiered interest rates
     */
    @GetMapping("/tiered-rates-info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTieredRatesInfo() {
        Map<String, Object> tieredRatesInfo = Map.of(
            "tier_1", Map.of(
                "threshold", 10000,
                "rate", 20,
                "daily_rate", 0.000548,
                "description", "First ₦10,000 at 20% p.a",
                "example", "₦10,000 earns ₦5.48 daily interest"
            ),
            "tier_2", Map.of(
                "threshold", 100000,
                "rate", 16,
                "daily_rate", 0.000438,
                "description", "₦10,001 - ₦100,000 at 16% p.a",
                "example", "₦90,000 earns ₦39.42 daily interest"
            ),
            "tier_3", Map.of(
                "rate", 8,
                "daily_rate", 0.000219,
                "description", "Above ₦100,000 at 8% p.a",
                "example", "₦50,000 earns ₦10.95 daily interest"
            ),
            "calculation_method", "Daily interest is calculated and credited automatically",
            "payout_frequency", "Daily at 11:00 AM",
            "minimum_balance", "No minimum balance required"
        );
        
        return ResponseEntity.ok(ApiResponse.success(tieredRatesInfo));
    }
    
    /**
     * Calculate tiered interest for a given balance
     */
    private BigDecimal calculateTieredInterest(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal remainingBalance = balance;
        BigDecimal totalInterest = BigDecimal.ZERO;
        
        // Tier 1: First 10,000 at 20% p.a
        if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier1Amount = remainingBalance.min(new BigDecimal("10000"));
            BigDecimal tier1Interest = tier1Amount.multiply(new BigDecimal("0.000548"));
            totalInterest = totalInterest.add(tier1Interest);
            remainingBalance = remainingBalance.subtract(tier1Amount);
        }
        
        // Tier 2: 10,001 - 100,000 at 16% p.a
        if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier2Amount = remainingBalance.min(new BigDecimal("90000"));
            BigDecimal tier2Interest = tier2Amount.multiply(new BigDecimal("0.000438"));
            totalInterest = totalInterest.add(tier2Interest);
            remainingBalance = remainingBalance.subtract(tier2Amount);
        }
        
        // Tier 3: Above 100,000 at 8% p.a
        if (remainingBalance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tier3Interest = remainingBalance.multiply(new BigDecimal("0.000219"));
            totalInterest = totalInterest.add(tier3Interest);
        }
        
        return totalInterest;
    }
    
    /**
     * Get tier breakdown for a given balance
     */
    private Map<String, Object> getTierBreakdown(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            return Map.of(
                "tier_1", Map.of("amount", 0, "rate", 20, "interest", 0),
                "tier_2", Map.of("amount", 0, "rate", 16, "interest", 0),
                "tier_3", Map.of("amount", 0, "rate", 8, "interest", 0),
                "total_interest", 0
            );
        }
        
        BigDecimal remainingBalance = balance;
        BigDecimal tier1Amount = remainingBalance.min(new BigDecimal("10000"));
        BigDecimal tier1Interest = tier1Amount.multiply(new BigDecimal("0.000548"));
        remainingBalance = remainingBalance.subtract(tier1Amount);
        
        BigDecimal tier2Amount = remainingBalance.min(new BigDecimal("90000"));
        BigDecimal tier2Interest = tier2Amount.multiply(new BigDecimal("0.000438"));
        remainingBalance = remainingBalance.subtract(tier2Amount);
        
        BigDecimal tier3Amount = remainingBalance;
        BigDecimal tier3Interest = tier3Amount.multiply(new BigDecimal("0.000219"));
        
        BigDecimal totalInterest = tier1Interest.add(tier2Interest).add(tier3Interest);
        
        return Map.of(
            "tier_1", Map.of(
                "amount", tier1Amount,
                "rate", 20,
                "interest", tier1Interest
            ),
            "tier_2", Map.of(
                "amount", tier2Amount,
                "rate", 16,
                "interest", tier2Interest
            ),
            "tier_3", Map.of(
                "amount", tier3Amount,
                "rate", 8,
                "interest", tier3Interest
            ),
            "total_interest", totalInterest
        );
    }
    
    /**
     * Request DTO for interest calculation
     */
    @Data
    public static class InterestCalculationRequestDTO {
        @NotNull(message = "Balance amount is required")
        @DecimalMin(value = "0", message = "Balance amount must be non-negative")
        @Digits(integer = 15, fraction = 4, message = "Balance amount must have at most 15 integer digits and 4 decimal places")
        private BigDecimal balanceAmount;
    }
}
