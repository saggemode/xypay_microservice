package com.xypay.xypay.controller;

import com.xypay.xypay.domain.SpendAndSaveAccount;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.dto.*;
import com.xypay.xypay.service.SpendAndSaveService;
import com.xypay.xypay.service.SpendAndSaveInterestService;
import com.xypay.xypay.service.SpendAndSaveNotificationService;
import com.xypay.xypay.repository.SpendAndSaveAccountRepository;
import com.xypay.xypay.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing Spend and Save accounts
 */
@RestController
@RequestMapping("/api/spend-and-save")
@Slf4j
public class SpendAndSaveAccountController {
    
    @Autowired
    private SpendAndSaveService spendAndSaveService;
    
    @Autowired
    private SpendAndSaveInterestService interestService;
    
    @Autowired
    private SpendAndSaveNotificationService notificationService;
    
    @Autowired
    private SpendAndSaveAccountRepository spendAndSaveAccountRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Activate Spend and Save for the current user
     */
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<SpendAndSaveAccountDTO>> activate(
            @Valid @RequestBody ActivateSpendAndSaveRequestDTO request,
            Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            SpendAndSaveAccount account = spendAndSaveService.activateSpendAndSave(
                user,
                request.getSavingsPercentage(),
                request.getFundSource(),
                request.getInitialAmount(),
                request.getWalletAmount(),
                request.getXySaveAmount()
            );
            
            SpendAndSaveAccountDTO accountDTO = new SpendAndSaveAccountDTO(account);
            
            return ResponseEntity.ok(ApiResponse.success(accountDTO, "Spend and Save activated successfully"));
            
        } catch (Exception e) {
            log.error("Error activating Spend and Save: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error activating Spend and Save: " + e.getMessage()));
        }
    }
    
    /**
     * Deactivate Spend and Save for the current user
     */
    @PostMapping("/deactivate")
    public ResponseEntity<ApiResponse<SpendAndSaveAccountDTO>> deactivate(
            @Valid @RequestBody DeactivateSpendAndSaveRequestDTO request,
            Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            SpendAndSaveAccount account = spendAndSaveService.deactivateSpendAndSave(user);
            SpendAndSaveAccountDTO accountDTO = new SpendAndSaveAccountDTO(account);
            
            return ResponseEntity.ok(ApiResponse.success(accountDTO, "Spend and Save deactivated successfully"));
            
        } catch (Exception e) {
            log.error("Error deactivating Spend and Save: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error deactivating Spend and Save: " + e.getMessage()));
        }
    }
    
    /**
     * Get comprehensive account summary
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SpendAndSaveAccountSummaryDTO>> getSummary(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> summary = spendAndSaveService.getAccountSummary(user);
            
            if (summary == null) {
                return ResponseEntity.notFound().build();
            }
            
            SpendAndSaveAccountSummaryDTO summaryDTO = new SpendAndSaveAccountSummaryDTO();
            summaryDTO.setAccount((SpendAndSaveAccountDTO) summary.get("account"));
            summaryDTO.setSettings((SpendAndSaveSettingsDTO) summary.get("settings"));
            summaryDTO.setInterestBreakdown((Map<String, Object>) summary.get("interest_breakdown"));
            summaryDTO.setRecentTransactions((List<SpendAndSaveTransactionDTO>) summary.get("recent_transactions"));
            
            return ResponseEntity.ok(ApiResponse.success(summaryDTO));
            
        } catch (Exception e) {
            log.error("Error getting account summary: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting account summary: " + e.getMessage()));
        }
    }
    
    /**
     * Get dashboard data for Spend and Save
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SpendAndSaveDashboardDTO>> getDashboard(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> summary = spendAndSaveService.getAccountSummary(user);
            if (summary == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Get interest forecast
            InterestForecastDTO forecast = interestService.getInterestForecast(user, 30);
            
            // Get tiered rates info
            Map<String, Object> tieredRatesInfo = Map.of(
                "tier_1", Map.of(
                    "threshold", 10000,
                    "rate", 20,
                    "description", "First ₦10,000 at 20% p.a"
                ),
                "tier_2", Map.of(
                    "threshold", 100000,
                    "rate", 16,
                    "description", "₦10,001 - ₦100,000 at 16% p.a"
                ),
                "tier_3", Map.of(
                    "rate", 8,
                    "description", "Above ₦100,000 at 8% p.a"
                )
            );
            
            // Get recent activity
            Optional<SpendAndSaveAccount> accountOpt = spendAndSaveAccountRepository.findByUser(user);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            SpendAndSaveAccount account = accountOpt.get();
            List<SpendAndSaveTransactionDTO> recentActivity = account.getTransactions().stream()
                .limit(5)
                .map(SpendAndSaveTransactionDTO::new)
                .collect(Collectors.toList());
            
            // Calculate savings progress
            Map<String, Object> savingsProgress = Map.of(
                "total_saved_from_spending", account.getTotalSavedFromSpending(),
                "total_interest_earned", account.getTotalInterestEarned(),
                "current_balance", account.getBalance(),
                "savings_percentage", account.getSavingsPercentage(),
                "total_transactions_processed", account.getTotalTransactionsProcessed()
            );
            
            SpendAndSaveDashboardDTO dashboard = new SpendAndSaveDashboardDTO();
            dashboard.setAccountSummary((SpendAndSaveAccountSummaryDTO) summary);
            dashboard.setInterestForecast(forecast);
            dashboard.setTieredRatesInfo(tieredRatesInfo);
            dashboard.setRecentActivity(recentActivity);
            dashboard.setSavingsProgress(savingsProgress);
            
            return ResponseEntity.ok(ApiResponse.success(dashboard));
            
        } catch (Exception e) {
            log.error("Error getting dashboard data: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting dashboard data: " + e.getMessage()));
        }
    }
    
    /**
     * Withdraw from Spend and Save account
     */
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<SpendAndSaveTransactionDTO>> withdraw(
            @Valid @RequestBody WithdrawFromSpendAndSaveRequestDTO request,
            Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            var withdrawalTx = spendAndSaveService.withdrawFromSpendAndSave(
                user,
                request.getAmount(),
                request.getDestination()
            );
            
            SpendAndSaveTransactionDTO transactionDTO = new SpendAndSaveTransactionDTO(withdrawalTx);
            
            return ResponseEntity.ok(ApiResponse.success(transactionDTO, 
                String.format("Successfully withdrew %s to %s", request.getAmount(), request.getDestination())));
            
        } catch (Exception e) {
            log.error("Error withdrawing from Spend and Save: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error withdrawing from Spend and Save: " + e.getMessage()));
        }
    }
    
    /**
     * Get interest forecast for the account
     */
    @GetMapping("/interest-forecast")
    public ResponseEntity<ApiResponse<InterestForecastDTO>> getInterestForecast(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            InterestForecastDTO forecast = interestService.getInterestForecast(user, 30);
            
            if (forecast == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(ApiResponse.success(forecast));
            
        } catch (Exception e) {
            log.error("Error getting interest forecast: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting interest forecast: " + e.getMessage()));
        }
    }
    
    /**
     * Get detailed interest breakdown by tier
     */
    @GetMapping("/interest-breakdown")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInterestBreakdown(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Optional<SpendAndSaveAccount> accountOpt = spendAndSaveAccountRepository.findByUser(user);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            SpendAndSaveAccount account = accountOpt.get();
            Map<String, Object> breakdown = new HashMap<>();
            // Parse the interest breakdown JSON string
            // This would need proper JSON parsing in a real implementation
            
            return ResponseEntity.ok(ApiResponse.success(breakdown));
            
        } catch (Exception e) {
            log.error("Error getting interest breakdown: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting interest breakdown: " + e.getMessage()));
        }
    }
}
