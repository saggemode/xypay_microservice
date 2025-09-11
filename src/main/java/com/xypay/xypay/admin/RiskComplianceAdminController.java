package com.xypay.xypay.admin;

import com.xypay.xypay.config.BranchEntityConfiguration;
import com.xypay.xypay.config.RiskComplianceConfiguration;
import com.xypay.xypay.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/risk-compliance")
public class RiskComplianceAdminController {
    
    @Autowired
    private ConfigurationService configurationService;
    
    // Risk & Compliance Configuration Endpoints
    
    @PostMapping("/compliance-config")
    public ResponseEntity<RiskComplianceConfiguration> createComplianceConfig(
            @RequestBody RiskComplianceConfiguration config) {
        RiskComplianceConfiguration savedConfig = configurationService.saveRiskComplianceConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/compliance-config/{configName}")
    public ResponseEntity<RiskComplianceConfiguration> getComplianceConfig(
            @PathVariable String configName) {
        RiskComplianceConfiguration config = configurationService.getRiskComplianceConfiguration(configName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/compliance-config")
    public ResponseEntity<List<RiskComplianceConfiguration>> getAllComplianceConfigs() {
        List<RiskComplianceConfiguration> configs = configurationService.getAllActiveRiskComplianceConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/compliance-config/type/{configType}")
    public ResponseEntity<List<RiskComplianceConfiguration>> getComplianceConfigsByType(
            @PathVariable String configType) {
        List<RiskComplianceConfiguration> configs = configurationService.getActiveConfigurationsByType(configType);
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/risk-config/{id}")
    public ResponseEntity<RiskComplianceConfiguration> updateRiskComplianceConfiguration(
            @PathVariable Long id,
            @RequestBody RiskComplianceConfiguration config) {
        // In a real implementation, we would update the existing record
        RiskComplianceConfiguration updatedConfig = configurationService.saveRiskComplianceConfiguration(config);
        return ResponseEntity.ok(updatedConfig);
    }
    
    @DeleteMapping("/risk-config/{id}")
    public ResponseEntity<Void> deleteRiskComplianceConfiguration(@PathVariable Long id) {
        // In a real implementation, we would delete the record
        return ResponseEntity.ok().build();
    }
    
    // Branch & Entity Configuration Endpoints
    
    @PostMapping("/branch-config")
    public ResponseEntity<BranchEntityConfiguration> createBranchConfig(
            @RequestBody BranchEntityConfiguration config) {
        BranchEntityConfiguration savedConfig = configurationService.saveBranchEntityConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/branch-config/{branchCode}")
    public ResponseEntity<BranchEntityConfiguration> getBranchConfig(
            @PathVariable String branchCode) {
        BranchEntityConfiguration config = configurationService.getBranchEntityConfiguration(branchCode);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/branch-config")
    public ResponseEntity<List<BranchEntityConfiguration>> getAllBranchConfigs() {
        List<BranchEntityConfiguration> configs = configurationService.getAllActiveBranchEntityConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/branch-config/type/{entityType}")
    public ResponseEntity<List<BranchEntityConfiguration>> getBranchConfigsByType(
            @PathVariable String entityType) {
        List<BranchEntityConfiguration> configs = configurationService.getActiveEntitiesByType(entityType);
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/branch-config/{id}")
    public ResponseEntity<BranchEntityConfiguration> updateBranchEntityConfiguration(
            @PathVariable Long id,
            @RequestBody BranchEntityConfiguration config) {
        // In a real implementation, we would update the existing record
        BranchEntityConfiguration updatedConfig = configurationService.saveBranchEntityConfiguration(config);
        return ResponseEntity.ok(updatedConfig);
    }
    
    @DeleteMapping("/branch-config/{id}")
    public ResponseEntity<Void> deleteBranchEntityConfiguration(@PathVariable Long id) {
        // In a real implementation, we would delete the record
        return ResponseEntity.ok().build();
    }
}