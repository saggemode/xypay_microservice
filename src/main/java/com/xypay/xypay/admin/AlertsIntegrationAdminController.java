package com.xypay.xypay.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/alerts-integration")
public class AlertsIntegrationAdminController {
    
    @Autowired
    private com.xypay.xypay.service.ConfigurationService configurationService;
    
    // Alert & Notification Configuration Endpoints
    
    @PostMapping("/alert-config")
    public ResponseEntity<com.xypay.xypay.domain.AlertNotificationConfiguration> createAlertConfig(
            @RequestBody com.xypay.xypay.domain.AlertNotificationConfiguration config) {
        com.xypay.xypay.domain.AlertNotificationConfiguration savedConfig = configurationService.saveAlertNotificationConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/alert-config/{alertName}")
    public ResponseEntity<com.xypay.xypay.domain.AlertNotificationConfiguration> getAlertConfig(
            @PathVariable String alertName) {
        com.xypay.xypay.domain.AlertNotificationConfiguration config = configurationService.getAlertNotificationConfiguration(alertName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/alert-config")
    public ResponseEntity<List<com.xypay.xypay.domain.AlertNotificationConfiguration>> getAllAlertConfigs() {
        List<com.xypay.xypay.domain.AlertNotificationConfiguration> configs = configurationService.getAllActiveAlertNotificationConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/alert-config/type/{alertType}")
    public ResponseEntity<List<com.xypay.xypay.domain.AlertNotificationConfiguration>> getAlertConfigsByType(
            @PathVariable String alertType) {
        List<com.xypay.xypay.domain.AlertNotificationConfiguration> configs = configurationService.getActiveAlertsByType(alertType);
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/alert-config/{id}")
    public ResponseEntity<com.xypay.xypay.domain.AlertNotificationConfiguration> updateAlertConfig(
            @PathVariable Long id,
            @RequestBody com.xypay.xypay.domain.AlertNotificationConfiguration config) {
        com.xypay.xypay.domain.AlertNotificationConfiguration updatedConfig = configurationService.updateAlertNotificationConfiguration(id, config);
        return ResponseEntity.ok(updatedConfig);
    }
    
    @DeleteMapping("/alert-config/{id}")
    public ResponseEntity<Void> deleteAlertConfig(@PathVariable Long id) {
        configurationService.deleteAlertNotificationConfiguration(id);
        return ResponseEntity.noContent().build();
    }
    
    // Integration Configuration Endpoints
    
    @PostMapping("/integration-config")
    public ResponseEntity<com.xypay.xypay.domain.IntegrationConfiguration> createIntegrationConfig(
            @RequestBody com.xypay.xypay.domain.IntegrationConfiguration config) {
        com.xypay.xypay.domain.IntegrationConfiguration savedConfig = configurationService.saveIntegrationConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/integration-config/{integrationName}")
    public ResponseEntity<com.xypay.xypay.domain.IntegrationConfiguration> getIntegrationConfig(
            @PathVariable String integrationName) {
        com.xypay.xypay.domain.IntegrationConfiguration config = configurationService.getIntegrationConfiguration(integrationName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/integration-config")
    public ResponseEntity<List<com.xypay.xypay.domain.IntegrationConfiguration>> getAllIntegrationConfigs() {
        List<com.xypay.xypay.domain.IntegrationConfiguration> configs = configurationService.getAllActiveIntegrationConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/integration-config/type/{integrationType}")
    public ResponseEntity<List<com.xypay.xypay.domain.IntegrationConfiguration>> getIntegrationConfigsByType(
            @PathVariable String integrationType) {
        List<com.xypay.xypay.domain.IntegrationConfiguration> configs = configurationService.getActiveIntegrationsByType(integrationType);
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/integration-config/{id}")
    public ResponseEntity<com.xypay.xypay.domain.IntegrationConfiguration> updateIntegrationConfig(
            @PathVariable Long id,
            @RequestBody com.xypay.xypay.domain.IntegrationConfiguration config) {
        com.xypay.xypay.domain.IntegrationConfiguration updatedConfig = configurationService.updateIntegrationConfiguration(id, config);
        return ResponseEntity.ok(updatedConfig);
    }
    
    @DeleteMapping("/integration-config/{id}")
    public ResponseEntity<Void> deleteIntegrationConfig(@PathVariable Long id) {
        configurationService.deleteIntegrationConfiguration(id);
        return ResponseEntity.noContent().build();
    }
}