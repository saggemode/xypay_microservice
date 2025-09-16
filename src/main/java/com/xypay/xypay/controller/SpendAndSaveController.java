package com.xypay.xypay.controller;

import com.xypay.xypay.domain.SpendAndSaveAccount;
import com.xypay.xypay.domain.SpendAndSaveTransaction;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.dto.*;
import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.repository.TransactionRepository;
import com.xypay.xypay.service.SpendAndSaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/spend-save")
@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERUSER')")
public class SpendAndSaveController {
    
    @Autowired
    private SpendAndSaveService spendAndSaveService;
    
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }
    
    /**
     * Show Spend and Save activation page
     */
    @GetMapping("/activate")
    public String showActivationPage(Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user already has a Spend and Save account
            Optional<SpendAndSaveAccount> existingAccountOpt = spendAndSaveService.getAccountByUser(currentUser);
            
            if (existingAccountOpt.isPresent()) {
                // User already has an account, show account details
                model.addAttribute("account", new SpendAndSaveAccountDTO(existingAccountOpt.get()));
                model.addAttribute("isExistingAccount", true);
            } else {
                // User doesn't have an account, show activation form
                model.addAttribute("isExistingAccount", false);
            }
            
            // Add user information
            model.addAttribute("user", currentUser);
            model.addAttribute("pageTitle", "Spend & Save Activation");
            
            return "spend-save/activation";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load activation page: " + e.getMessage());
            return "spend-save/activation";
        }
    }
    
    /**
     * Process Spend and Save activation
     */
    @PostMapping("/activate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> activateSpendAndSave(
            @RequestParam BigDecimal savingsPercentage,
            @RequestParam String fundSource,
            @RequestParam(required = false) BigDecimal initialAmount,
            @RequestParam(required = false) BigDecimal walletAmount,
            @RequestParam(required = false) BigDecimal xySaveAmount,
            @RequestParam(required = false) Boolean emailNotifications,
            @RequestParam(required = false) Boolean smsNotifications,
            @RequestParam(required = false) Boolean pushNotifications,
            @RequestParam(required = false) String withdrawalDestination,
            @RequestParam(required = false) BigDecimal autoWithdrawalThreshold) {
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            // Activate Spend and Save account
            SpendAndSaveAccount account = spendAndSaveService.activateSpendAndSave(
                currentUser, savingsPercentage, fundSource, initialAmount, walletAmount, xySaveAmount);
            
            // Save notification preferences
            Map<String, Object> settingsData = new HashMap<>();
            if (emailNotifications != null) settingsData.put("email_notifications", emailNotifications);
            if (smsNotifications != null) settingsData.put("sms_notifications", smsNotifications);
            if (pushNotifications != null) settingsData.put("push_notifications", pushNotifications);
            if (withdrawalDestination != null) settingsData.put("default_withdrawal_destination", withdrawalDestination);
            if (autoWithdrawalThreshold != null) settingsData.put("auto_withdrawal_threshold", autoWithdrawalThreshold);
            
            if (!settingsData.isEmpty()) {
                spendAndSaveService.updateSettings(currentUser, settingsData);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Spend and Save activated successfully!");
            response.put("account", new SpendAndSaveAccountDTO(account));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to activate Spend and Save: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get user's Spend and Save account details
     */
    @GetMapping("/account")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAccountDetails() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Optional<SpendAndSaveAccount> accountOpt = spendAndSaveService.getAccountByUser(currentUser);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", false, "message", "No Spend and Save account found"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("account", new SpendAndSaveAccountDTO(accountOpt.get()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get account details: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get comprehensive account summary
     */
    @GetMapping("/summary")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAccountSummary() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Map<String, Object> summary = spendAndSaveService.getAccountSummary(currentUser);
            if (summary == null) {
                return ResponseEntity.ok(Map.of("success", false, "message", "No Spend and Save account found"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("summary", summary);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get account summary: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get dashboard data
     */
    @GetMapping("/dashboard")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Map<String, Object> summary = spendAndSaveService.getAccountSummary(currentUser);
            if (summary == null) {
                return ResponseEntity.ok(Map.of("success", false, "message", "No Spend and Save account found"));
            }
            
            // Get interest forecast
            Map<String, Object> interestForecast = getInterestForecast(currentUser);
            
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
            
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("account_summary", summary);
            dashboardData.put("interest_forecast", interestForecast);
            dashboardData.put("tiered_rates_info", tieredRatesInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("dashboard", dashboardData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get dashboard data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Withdraw from Spend and Save account
     */
    @PostMapping("/withdraw")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> withdrawFromSpendAndSave(
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "wallet") String destination) {
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            SpendAndSaveTransaction withdrawalTx = spendAndSaveService.withdrawFromSpendAndSave(
                currentUser, amount, destination);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("Successfully withdrew %s to %s", amount, destination));
            response.put("transaction", new SpendAndSaveTransactionDTO(withdrawalTx));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to withdraw: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get interest forecast
     */
    @GetMapping("/interest-forecast")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getInterestForecast() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Map<String, Object> forecast = getInterestForecast(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("forecast", forecast);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get interest forecast: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    private Map<String, Object> getInterestForecast(User user) {
        try {
            Optional<SpendAndSaveAccount> accountOpt = spendAndSaveService.getAccountByUser(user);
            if (accountOpt.isEmpty()) {
                return Map.of(
                    "daily_interest", BigDecimal.ZERO,
                    "monthly_interest", BigDecimal.ZERO,
                    "annual_interest", BigDecimal.ZERO,
                    "forecast_days", 30
                );
            }
            
            SpendAndSaveAccount account = accountOpt.get();
            if (account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                return Map.of(
                    "daily_interest", BigDecimal.ZERO,
                    "monthly_interest", BigDecimal.ZERO,
                    "annual_interest", BigDecimal.ZERO,
                    "forecast_days", 30
                );
            }
            
            // Calculate daily interest
            BigDecimal dailyInterest = account.calculateTieredInterest();
            BigDecimal monthlyInterest = dailyInterest.multiply(new BigDecimal("30"));
            BigDecimal annualInterest = dailyInterest.multiply(new BigDecimal("365"));
            
            return Map.of(
                "daily_interest", dailyInterest,
                "monthly_interest", monthlyInterest,
                "annual_interest", annualInterest,
                "forecast_days", 30,
                "current_balance", account.getBalance(),
                "interest_breakdown", account.getInterestBreakdown()
            );
            
        } catch (Exception e) {
            return Map.of(
                "daily_interest", BigDecimal.ZERO,
                "monthly_interest", BigDecimal.ZERO,
                "annual_interest", BigDecimal.ZERO,
                "forecast_days", 30
            );
        }
    }
    
    /**
     * Process a spending transaction for auto-save
     */
    @PostMapping("/process-spending")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processSpendingTransaction(
            @RequestParam UUID transactionId) {
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
            if (transactionOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Transaction not found"));
            }
            
            Transaction transaction = transactionOpt.get();
            if (!"debit".equals(transaction.getType())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Only debit transactions can be processed for auto-save"));
            }
            
            SpendAndSaveTransaction autoSaveTx = spendAndSaveService.processSpendingTransaction(transaction);
            
            if (autoSaveTx == null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "No auto-save processed (account inactive or amount below threshold)"
                ));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Auto-save processed successfully");
            response.put("transaction", new SpendAndSaveTransactionDTO(autoSaveTx));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to process spending transaction: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Credit daily interest
     */
    @PostMapping("/credit-interest")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> creditInterest() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            SpendAndSaveTransaction interestTx = spendAndSaveService.calculateAndCreditInterest(currentUser);
            
            if (interestTx == null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "No interest to credit (zero balance)"
                ));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Interest credited successfully");
            response.put("transaction", new SpendAndSaveTransactionDTO(interestTx));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to credit interest: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Deactivate Spend and Save
     */
    @PostMapping("/deactivate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deactivateSpendAndSave() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            SpendAndSaveAccount account = spendAndSaveService.deactivateSpendAndSave(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Spend and Save deactivated successfully!");
            response.put("account", new SpendAndSaveAccountDTO(account));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to deactivate Spend and Save: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get user preferences (API endpoint)
     */
    @GetMapping("/api/settings")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserSettings() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Map<String, Object> summary = spendAndSaveService.getAccountSummary(currentUser);
            if (summary == null) {
                return ResponseEntity.ok(Map.of("success", false, "message", "No Spend and Save account found"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("preferences", summary.get("settings"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get user settings: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Update Spend and Save settings
     */
    @PutMapping("/settings/spend-save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateSpendSaveSettings(
            @RequestParam BigDecimal savingsPercentage,
            @RequestParam BigDecimal minTransactionAmount,
            @RequestParam String fundingSource,
            @RequestParam(required = false) Boolean autoWithdrawalEnabled,
            @RequestParam(required = false) BigDecimal autoWithdrawalThreshold,
            @RequestParam(required = false) String withdrawalDestination) {
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Map<String, Object> settingsData = new HashMap<>();
            settingsData.put("preferred_savings_percentage", savingsPercentage);
            settingsData.put("min_transaction_threshold", minTransactionAmount);
            settingsData.put("funding_preference", fundingSource);
            if (autoWithdrawalEnabled != null) settingsData.put("auto_withdrawal_enabled", autoWithdrawalEnabled);
            if (autoWithdrawalThreshold != null) settingsData.put("auto_withdrawal_threshold", autoWithdrawalThreshold);
            if (withdrawalDestination != null) settingsData.put("default_withdrawal_destination", withdrawalDestination);
            
            spendAndSaveService.updateSettings(currentUser, settingsData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Spend and Save settings updated successfully!");
            response.put("preferences", spendAndSaveService.getAccountSummary(currentUser).get("settings"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update Spend and Save settings: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Update notification settings
     */
    @PutMapping("/settings/notifications")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateNotificationSettings(
            @RequestParam(required = false) Boolean emailNotifications,
            @RequestParam(required = false) Boolean smsNotifications,
            @RequestParam(required = false) Boolean pushNotifications,
            @RequestParam(required = false) Boolean spendSaveNotifications,
            @RequestParam(required = false) Boolean interestNotifications,
            @RequestParam(required = false) Boolean milestoneNotifications) {
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Map<String, Object> settingsData = new HashMap<>();
            if (emailNotifications != null) settingsData.put("email_notifications", emailNotifications);
            if (smsNotifications != null) settingsData.put("sms_notifications", smsNotifications);
            if (pushNotifications != null) settingsData.put("push_notifications", pushNotifications);
            if (spendSaveNotifications != null) settingsData.put("auto_save_notifications", spendSaveNotifications);
            if (interestNotifications != null) settingsData.put("interest_notifications", interestNotifications);
            if (milestoneNotifications != null) settingsData.put("milestone_notifications", milestoneNotifications);
            
            spendAndSaveService.updateSettings(currentUser, settingsData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification settings updated successfully!");
            response.put("preferences", spendAndSaveService.getAccountSummary(currentUser).get("settings"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update notification settings: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Toggle Spend and Save on/off
     */
    @PostMapping("/settings/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSpendSave(@RequestParam Boolean enabled) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            if (enabled) {
                // Activate with default settings
                spendAndSaveService.activateSpendAndSave(currentUser, new BigDecimal("5.00"), "wallet", null, null, null);
            } else {
                // Deactivate
                spendAndSaveService.deactivateSpendAndSave(currentUser);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", enabled ? "Spend and Save enabled!" : "Spend and Save disabled!");
            response.put("preferences", spendAndSaveService.getAccountSummary(currentUser).get("settings"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to toggle Spend and Save: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Show settings management page
     */
    @GetMapping("/settings")
    public String showSettingsPage(Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get user preferences
            Map<String, Object> summary = spendAndSaveService.getAccountSummary(currentUser);
            if (summary != null) {
                model.addAttribute("preferences", summary.get("settings"));
            }
            model.addAttribute("user", currentUser);
            model.addAttribute("pageTitle", "Spend & Save Settings");
            
            return "spend-save/settings";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load settings page: " + e.getMessage());
            return "spend-save/settings";
        }
    }
    
    /**
     * Save all settings (for the new settings page)
     */
    @PostMapping("/settings/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveSettings(@RequestBody Map<String, Object> formData) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            // Update settings with all form data
            spendAndSaveService.updateSettings(currentUser, formData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Settings saved successfully!");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to save settings: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}