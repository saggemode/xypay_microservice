package com.xypay.xypay.controller;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.UserRepository;
import com.xypay.xypay.service.UserPreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/preferences")
public class UserPreferencesController {
    
    @Autowired
    private UserPreferencesService userPreferencesService;
    
    @Autowired
    private UserRepository userRepository;

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

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPreferences() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            Map<String, Object> prefsMap = userPreferencesService.getPreferencesAsMap(currentUser.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("preferences", prefsMap);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get preferences: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> savePreferences(@RequestBody Map<String, Object> preferencesData) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not authenticated"));
            }
            
            // Extract preferences from request body
            Boolean spendSaveEnabled = (Boolean) preferencesData.get("spendSaveEnabled");
            Double savingsPercentage = (Double) preferencesData.get("savingsPercentage");
            Double minTransactionAmount = (Double) preferencesData.get("minTransactionAmount");
            String fundingSource = (String) preferencesData.get("fundingSource");
            Boolean autoWithdrawalEnabled = (Boolean) preferencesData.get("autoWithdrawalEnabled");
            Double autoWithdrawalThreshold = (Double) preferencesData.get("autoWithdrawalThreshold");
            String withdrawalDestination = (String) preferencesData.get("withdrawalDestination");
            
            Boolean emailNotifications = (Boolean) preferencesData.get("emailNotifications");
            Boolean smsNotifications = (Boolean) preferencesData.get("smsNotifications");
            Boolean pushNotifications = (Boolean) preferencesData.get("pushNotifications");
            Boolean spendSaveNotifications = (Boolean) preferencesData.get("spendSaveNotifications");
            Boolean interestNotifications = (Boolean) preferencesData.get("interestNotifications");
            Boolean milestoneNotifications = (Boolean) preferencesData.get("milestoneNotifications");
            
            // Update Spend and Save preferences
            if (savingsPercentage != null || minTransactionAmount != null || fundingSource != null) {
                userPreferencesService.updateSpendSavePreferences(
                    currentUser.getId(),
                    savingsPercentage != null ? java.math.BigDecimal.valueOf(savingsPercentage) : null,
                    minTransactionAmount != null ? java.math.BigDecimal.valueOf(minTransactionAmount) : null,
                    fundingSource,
                    autoWithdrawalEnabled,
                    autoWithdrawalThreshold != null ? java.math.BigDecimal.valueOf(autoWithdrawalThreshold) : null,
                    withdrawalDestination
                );
            }
            
            // Update notification preferences
            if (emailNotifications != null || smsNotifications != null || pushNotifications != null ||
                spendSaveNotifications != null || interestNotifications != null || milestoneNotifications != null) {
                userPreferencesService.updateNotificationPreferences(
                    currentUser.getId(),
                    emailNotifications,
                    smsNotifications,
                    pushNotifications,
                    spendSaveNotifications,
                    interestNotifications,
                    milestoneNotifications
                );
            }
            
            // Toggle Spend and Save if needed
            if (spendSaveEnabled != null) {
                userPreferencesService.toggleSpendSave(currentUser.getId(), spendSaveEnabled);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Preferences saved successfully!");
            response.put("preferences", userPreferencesService.getPreferencesAsMap(currentUser.getId()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to save preferences: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
