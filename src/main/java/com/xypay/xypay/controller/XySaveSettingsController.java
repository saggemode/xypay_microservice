package com.xypay.xypay.controller;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.XySaveSettings;
import com.xypay.xypay.dto.XySaveSettingsDTO;
import com.xypay.xypay.repository.XySaveSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/xysave/settings")
public class XySaveSettingsController {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveSettingsController.class);
    
    @Autowired
    private XySaveSettingsRepository xySaveSettingsRepository;
    
    /**
     * Get XySave settings for current user
     */
    @GetMapping
    public ResponseEntity<XySaveSettingsDTO> getSettings(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            var settings = xySaveSettingsRepository.findByUser(user);
            
            if (settings.isPresent()) {
                return ResponseEntity.ok(new XySaveSettingsDTO(settings.get()));
            } else {
                // Create default settings if not found
                XySaveSettings defaultSettings = new XySaveSettings();
                defaultSettings.setUser(user);
                XySaveSettings savedSettings = xySaveSettingsRepository.save(defaultSettings);
                return ResponseEntity.ok(new XySaveSettingsDTO(savedSettings));
            }
        } catch (Exception e) {
            logger.error("Error getting XySave settings: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update XySave settings
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody XySaveSettingsDTO request, 
                                                              Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            var settings = xySaveSettingsRepository.findByUser(user);
            
            XySaveSettings settingsToUpdate;
            if (settings.isPresent()) {
                settingsToUpdate = settings.get();
            } else {
                settingsToUpdate = new XySaveSettings();
                settingsToUpdate.setUser(user);
            }
            
            // Update fields
            if (request.getDailyInterestNotifications() != null) {
                settingsToUpdate.setDailyInterestNotifications(request.getDailyInterestNotifications());
            }
            if (request.getGoalReminders() != null) {
                settingsToUpdate.setGoalReminders(request.getGoalReminders());
            }
            if (request.getAutoSaveNotifications() != null) {
                settingsToUpdate.setAutoSaveNotifications(request.getAutoSaveNotifications());
            }
            if (request.getInvestmentUpdates() != null) {
                settingsToUpdate.setInvestmentUpdates(request.getInvestmentUpdates());
            }
            if (request.getPreferredInterestPayout() != null) {
                settingsToUpdate.setPreferredInterestPayout(
                    XySaveSettings.InterestPayoutFrequency.valueOf(request.getPreferredInterestPayout()));
            }
            
            XySaveSettings savedSettings = xySaveSettingsRepository.save(settingsToUpdate);
            
            Map<String, Object> response = Map.of(
                "message", "Settings updated successfully",
                "settings", new XySaveSettingsDTO(savedSettings)
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating XySave settings: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update settings"));
        }
    }
    
    /**
     * Get available interest payout frequencies
     */
    @GetMapping("/interest-payout-frequencies")
    public ResponseEntity<Map<String, String[]>> getInterestPayoutFrequencies() {
        try {
            Map<String, String[]> frequencies = Map.of(
                "frequencies", new String[]{
                    "DAILY", "WEEKLY", "MONTHLY"
                },
                "descriptions", new String[]{
                    "Daily", "Weekly", "Monthly"
                }
            );
            return ResponseEntity.ok(frequencies);
        } catch (Exception e) {
            logger.error("Error getting interest payout frequencies: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
