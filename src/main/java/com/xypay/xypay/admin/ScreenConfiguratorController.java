package com.xypay.xypay.admin;

import com.xypay.xypay.domain.ScreenConfiguration;
import com.xypay.xypay.service.ScreenConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// @RestController
// @RequestMapping("/admin/screen-configurator")
public class ScreenConfiguratorController {
    
    @Autowired
    private ScreenConfigurationService screenConfigurationService;
    
    /**
     * Create a new screen configuration
     */
    @PostMapping("/screen-config")
    public ResponseEntity<Map<String, Object>> createScreenConfig(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = screenConfigurationService.createScreenConfiguration(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get screen configuration by code
     */
    @GetMapping("/screen-config/{screenCode}")
    public ResponseEntity<ScreenConfiguration> getScreenConfig(@PathVariable String screenCode) {
        ScreenConfiguration config = screenConfigurationService.getScreenConfiguration(screenCode);
        return config != null ? ResponseEntity.ok(config) : ResponseEntity.notFound().build();
    }
    
    /**
     * Get all screen configurations
     */
    @GetMapping("/screen-config")
    public ResponseEntity<List<ScreenConfiguration>> getAllScreenConfigs() {
        List<ScreenConfiguration> configs = screenConfigurationService.getAllScreenConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    /**
     * Update screen configuration
     */
    @PutMapping("/screen-config/{id}")
    public ResponseEntity<Map<String, Object>> updateScreenConfig(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID configId = new UUID(0L, id); // Create UUID from Long
        Map<String, Object> response = screenConfigurationService.updateScreenConfiguration(configId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete screen configuration
     */
    @DeleteMapping("/screen-config/{id}")
    public ResponseEntity<Map<String, Object>> deleteScreenConfig(@PathVariable Long id) {
        // Convert Long to UUID - this is a workaround for the ID type mismatch
        UUID configId = new UUID(0L, id); // Create UUID from Long
        Map<String, Object> response = screenConfigurationService.deleteScreenConfiguration(configId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Generate dynamic form based on screen configuration
     */
    @GetMapping("/generate-form/{screenCode}")
    public ResponseEntity<Map<String, Object>> generateForm(@PathVariable String screenCode) {
        Map<String, Object> formConfig = screenConfigurationService.generateDynamicForm(screenCode);
        return ResponseEntity.ok(formConfig);
    }
    
    /**
     * Validate form data against screen configuration
     */
    @PostMapping("/validate-form/{screenCode}")
    public ResponseEntity<Map<String, Object>> validateForm(@PathVariable String screenCode, @RequestBody Map<String, Object> formData) {
        Map<String, Object> validationResult = screenConfigurationService.validateFormData(screenCode, formData);
        return ResponseEntity.ok(validationResult);
    }
    
    /**
     * Preview screen configuration
     */
    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewScreen(@RequestBody Map<String, Object> configData) {
        Map<String, Object> preview = screenConfigurationService.previewScreenConfiguration(configData);
        return ResponseEntity.ok(preview);
    }
}
