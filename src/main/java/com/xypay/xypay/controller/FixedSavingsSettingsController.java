package com.xypay.xypay.controller;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.dto.FixedSavingsSettingsDTO;
import com.xypay.xypay.service.FixedSavingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fixed-savings/settings")
public class FixedSavingsSettingsController {
    
    @Autowired
    private FixedSavingsService fixedSavingsService;
    
    /**
     * Get current user's settings
     */
    @GetMapping("/my-settings")
    public ResponseEntity<FixedSavingsSettingsDTO> getCurrentUserSettings() {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        FixedSavingsSettingsDTO settings = fixedSavingsService.getFixedSavingsSettings(currentUser);
        return ResponseEntity.ok(settings);
    }
    
    /**
     * Update notification preferences
     */
    @PostMapping("/update-notifications")
    public ResponseEntity<FixedSavingsSettingsDTO> updateNotificationPreferences(
            @RequestBody FixedSavingsSettingsDTO settingsDTO) {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        // In a real implementation, you would update only notification preferences
        FixedSavingsSettingsDTO updatedSettings = fixedSavingsService.updateFixedSavingsSettings(settingsDTO, currentUser);
        return ResponseEntity.ok(updatedSettings);
    }
    
    /**
     * Update user preferences
     */
    @PostMapping("/update-preferences")
    public ResponseEntity<FixedSavingsSettingsDTO> updateUserPreferences(
            @RequestBody FixedSavingsSettingsDTO settingsDTO) {
        // In a real implementation, you would get the current user from security context
        User currentUser = new User();
        currentUser.setId(1L);
        
        // In a real implementation, you would update only user preferences
        FixedSavingsSettingsDTO updatedSettings = fixedSavingsService.updateFixedSavingsSettings(settingsDTO, currentUser);
        return ResponseEntity.ok(updatedSettings);
    }
}