package com.xypay.xypay.controller;

import com.xypay.xypay.domain.SpendAndSaveSettings;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.dto.ApiResponse;
import com.xypay.xypay.dto.SpendAndSaveSettingsDTO;
import com.xypay.xypay.dto.UpdateSpendAndSaveSettingsRequestDTO;
import com.xypay.xypay.service.SpendAndSaveService;
import com.xypay.xypay.repository.SpendAndSaveSettingsRepository;
import com.xypay.xypay.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing Spend and Save settings
 */
@RestController
@RequestMapping("/api/spend-and-save/settings")
@Slf4j
public class SpendAndSaveSettingsController {
    
    @Autowired
    private SpendAndSaveService spendAndSaveService;
    
    @Autowired
    private SpendAndSaveSettingsRepository spendAndSaveSettingsRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get current user's Spend and Save settings
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SpendAndSaveSettingsDTO>> getSettings(Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            Optional<SpendAndSaveSettings> settingsOpt = spendAndSaveSettingsRepository.findByUser(user);
            if (settingsOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            SpendAndSaveSettingsDTO settingsDTO = new SpendAndSaveSettingsDTO(settingsOpt.get());
            return ResponseEntity.ok(ApiResponse.success(settingsDTO));
            
        } catch (Exception e) {
            log.error("Error getting settings: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error getting settings: " + e.getMessage()));
        }
    }
    
    /**
     * Update Spend and Save settings
     */
    @PatchMapping
    public ResponseEntity<ApiResponse<SpendAndSaveSettingsDTO>> updateSettings(
            @Valid @RequestBody UpdateSpendAndSaveSettingsRequestDTO request,
            Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Convert request to map for service method
            Map<String, Object> settingsData = new HashMap<>();
            
            if (request.getAutoSaveNotifications() != null) {
                settingsData.put("auto_save_notifications", request.getAutoSaveNotifications());
            }
            if (request.getInterestNotifications() != null) {
                settingsData.put("interest_notifications", request.getInterestNotifications());
            }
            if (request.getWithdrawalNotifications() != null) {
                settingsData.put("withdrawal_notifications", request.getWithdrawalNotifications());
            }
            if (request.getPreferredSavingsPercentage() != null) {
                settingsData.put("preferred_savings_percentage", request.getPreferredSavingsPercentage());
            }
            if (request.getMinTransactionThreshold() != null) {
                settingsData.put("min_transaction_threshold", request.getMinTransactionThreshold());
            }
            if (request.getDefaultWithdrawalDestination() != null) {
                settingsData.put("default_withdrawal_destination", request.getDefaultWithdrawalDestination());
            }
            if (request.getInterestPayoutFrequency() != null) {
                settingsData.put("interest_payout_frequency", request.getInterestPayoutFrequency());
            }
            if (request.getFundingPreference() != null) {
                settingsData.put("funding_preference", request.getFundingPreference());
            }
            if (request.getAutoWithdrawalEnabled() != null) {
                settingsData.put("auto_withdrawal_enabled", request.getAutoWithdrawalEnabled());
            }
            if (request.getAutoWithdrawalThreshold() != null) {
                settingsData.put("auto_withdrawal_threshold", request.getAutoWithdrawalThreshold());
            }
            
            SpendAndSaveSettings settings = spendAndSaveService.updateSettings(user, settingsData);
            SpendAndSaveSettingsDTO settingsDTO = new SpendAndSaveSettingsDTO(settings);
            
            return ResponseEntity.ok(ApiResponse.success(settingsDTO, "Settings updated successfully"));
            
        } catch (Exception e) {
            log.error("Error updating settings: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error updating settings: " + e.getMessage()));
        }
    }
}
