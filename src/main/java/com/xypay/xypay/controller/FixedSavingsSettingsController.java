package com.xypay.xypay.controller;

import com.xypay.xypay.domain.FixedSavingsSettings;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.dto.ApiResponse;
import com.xypay.xypay.dto.FixedSavingsSettingsDTO;
import com.xypay.xypay.repository.FixedSavingsSettingsRepository;
import com.xypay.xypay.service.FixedSavingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/fixed-savings/settings")
@PreAuthorize("hasRole('USER')")
public class FixedSavingsSettingsController {
    
    @Autowired
    private FixedSavingsService fixedSavingsService;
    
    @Autowired
    private FixedSavingsSettingsRepository fixedSavingsSettingsRepository;
    
    /**
     * Get all settings for the current user
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FixedSavingsSettingsDTO>>> getUserFixedSavingsSettings(
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            List<FixedSavingsSettings> settings = fixedSavingsSettingsRepository.findByUser(currentUser);
            
            List<FixedSavingsSettingsDTO> settingsDTOs = settings.stream()
                .map(this::convertToDTO)
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(settingsDTOs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve fixed savings settings"));
        }
    }
    
    /**
     * Get current user's settings
     */
    @GetMapping("/my-settings")
    public ResponseEntity<ApiResponse<FixedSavingsSettingsDTO>> getMySettings(
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FixedSavingsSettingsDTO settings = fixedSavingsService.getFixedSavingsSettings(currentUser);
            return ResponseEntity.ok(ApiResponse.success(settings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve user settings"));
        }
    }
    
    /**
     * Create settings for the current user
     */
    @PostMapping
    public ResponseEntity<ApiResponse<FixedSavingsSettingsDTO>> createFixedSavingsSettings(
            @Valid @RequestBody FixedSavingsSettingsDTO settingsDTO, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FixedSavingsSettingsDTO createdSettings = fixedSavingsService.updateFixedSavingsSettings(settingsDTO, currentUser);
            return ResponseEntity.ok(ApiResponse.success(createdSettings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create fixed savings settings"));
        }
    }
    
    /**
     * Update settings for the current user
     */
    @PutMapping
    public ResponseEntity<ApiResponse<FixedSavingsSettingsDTO>> updateFixedSavingsSettings(
            @Valid @RequestBody FixedSavingsSettingsDTO settingsDTO, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            FixedSavingsSettingsDTO updatedSettings = fixedSavingsService.updateFixedSavingsSettings(settingsDTO, currentUser);
            return ResponseEntity.ok(ApiResponse.success(updatedSettings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update fixed savings settings"));
        }
    }
    
    /**
     * Update notification preferences
     */
    @PostMapping("/update-notifications")
    public ResponseEntity<ApiResponse<FixedSavingsSettingsDTO>> updateNotifications(
            @RequestBody FixedSavingsSettingsDTO settingsDTO, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Get existing settings or create new ones
            List<FixedSavingsSettings> settingsList = fixedSavingsSettingsRepository.findByUser(currentUser);
            FixedSavingsSettings settings = settingsList.isEmpty() ? null : settingsList.get(0);
            if (settings == null) {
                settings = new FixedSavingsSettings();
                settings.setUser(currentUser);
            }
            
            // Update notification preferences
            if (settingsDTO.getMaturityNotifications() != null) {
                settings.setMaturityNotifications(settingsDTO.getMaturityNotifications());
            }
            if (settingsDTO.getInterestNotifications() != null) {
                settings.setInterestNotifications(settingsDTO.getInterestNotifications());
            }
            if (settingsDTO.getAutoRenewalNotifications() != null) {
                settings.setAutoRenewalNotifications(settingsDTO.getAutoRenewalNotifications());
            }
            
            FixedSavingsSettings savedSettings = fixedSavingsSettingsRepository.save(settings);
            FixedSavingsSettingsDTO responseDTO = convertToDTO(savedSettings);
            
            return ResponseEntity.ok(ApiResponse.success(responseDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update notification preferences"));
        }
    }
    
    /**
     * Update user preferences
     */
    @PostMapping("/update-preferences")
    public ResponseEntity<ApiResponse<FixedSavingsSettingsDTO>> updatePreferences(
            @RequestBody FixedSavingsSettingsDTO settingsDTO, Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            
            // Get existing settings or create new ones
            List<FixedSavingsSettings> settingsList = fixedSavingsSettingsRepository.findByUser(currentUser);
            FixedSavingsSettings settings = settingsList.isEmpty() ? null : settingsList.get(0);
            if (settings == null) {
                settings = new FixedSavingsSettings();
                settings.setUser(currentUser);
            }
            
            // Update preferences
            if (settingsDTO.getDefaultAutoRenewal() != null) {
                settings.setDefaultAutoRenewal(settingsDTO.getDefaultAutoRenewal());
            }
            if (settingsDTO.getDefaultRenewalDuration() != null) {
                settings.setDefaultRenewalDuration(settingsDTO.getDefaultRenewalDuration());
            }
            if (settingsDTO.getDefaultSource() != null) {
                try {
                    FixedSavingsSettings.Source source = FixedSavingsSettings.Source.valueOf(settingsDTO.getDefaultSource());
                    settings.setDefaultSource(source);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid source value"));
                }
            }
            
            FixedSavingsSettings savedSettings = fixedSavingsSettingsRepository.save(settings);
            FixedSavingsSettingsDTO responseDTO = convertToDTO(savedSettings);
            
            return ResponseEntity.ok(ApiResponse.success(responseDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update preferences"));
        }
    }
    
    private User getCurrentUser(Authentication authentication) {
        // Implementation to get current user from authentication
        // This would depend on your security configuration
        return new User(); // Placeholder
    }
    
    private FixedSavingsSettingsDTO convertToDTO(FixedSavingsSettings settings) {
        FixedSavingsSettingsDTO dto = new FixedSavingsSettingsDTO();
        dto.setId(settings.getId());
        dto.setUser(settings.getUser().getUsername());
        dto.setMaturityNotifications(settings.getMaturityNotifications());
        dto.setInterestNotifications(settings.getInterestNotifications());
        dto.setAutoRenewalNotifications(settings.getAutoRenewalNotifications());
        dto.setDefaultAutoRenewal(settings.getDefaultAutoRenewal());
        dto.setDefaultRenewalDuration(settings.getDefaultRenewalDuration());
        dto.setDefaultSource(settings.getDefaultSource().name());
        dto.setCreatedAt(settings.getCreatedAt());
        dto.setUpdatedAt(settings.getUpdatedAt());
        return dto;
    }
}