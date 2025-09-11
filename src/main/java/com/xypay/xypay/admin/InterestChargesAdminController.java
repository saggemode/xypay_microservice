package com.xypay.xypay.admin;

import com.xypay.xypay.config.ChargeConfiguration;
import com.xypay.xypay.config.InterestConfiguration;
import com.xypay.xypay.config.TransactionRuleConfiguration;
import com.xypay.xypay.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/admin/interest-charges")
public class InterestChargesAdminController {
    
    @Autowired
    private ConfigurationService configurationService;
    
    // Interest Configuration Endpoints
    
    @PostMapping("/interest-config")
    public ResponseEntity<InterestConfiguration> createInterestConfig(
            @RequestBody InterestConfiguration config) {
        InterestConfiguration savedConfig = configurationService.saveInterestConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/interest-config/{configName}")
    public ResponseEntity<InterestConfiguration> getInterestConfig(
            @PathVariable String configName) {
        InterestConfiguration config = configurationService.getInterestConfiguration(configName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/interest-config")
    public ResponseEntity<List<InterestConfiguration>> getAllInterestConfigs() {
        List<InterestConfiguration> configs = configurationService.getAllActiveInterestConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/interest-config/{id}")
    public ResponseEntity<InterestConfiguration> updateInterestConfiguration(
            @PathVariable Long id,
            @RequestBody InterestConfiguration config) {
        // In a real implementation, we would update the existing record
        InterestConfiguration updatedConfig = configurationService.saveInterestConfiguration(config);
        return ResponseEntity.ok(updatedConfig);
    }
    
    @DeleteMapping("/interest-config/{id}")
    public ResponseEntity<Void> deleteInterestConfiguration(@PathVariable Long id) {
        // In a real implementation, we would delete the record
        return ResponseEntity.ok().build();
    }
    
    // Charge Configuration Endpoints
    
    @PostMapping("/charge-config")
    public ResponseEntity<ChargeConfiguration> createChargeConfig(
            @RequestBody ChargeConfiguration config) {
        ChargeConfiguration savedConfig = configurationService.saveChargeConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/charge-config/{chargeName}")
    public ResponseEntity<ChargeConfiguration> getChargeConfig(
            @PathVariable String chargeName) {
        ChargeConfiguration config = configurationService.getChargeConfiguration(chargeName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/charge-config")
    public ResponseEntity<List<ChargeConfiguration>> getAllChargeConfigs() {
        // In a real implementation, we would get all charge configurations
        List<ChargeConfiguration> configs = configurationService.getActiveChargesByType("MAINTENANCE");
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/charge-config/type/{chargeType}")
    public ResponseEntity<List<ChargeConfiguration>> getChargeConfigsByType(
            @PathVariable String chargeType) {
        List<ChargeConfiguration> configs = configurationService.getActiveChargesByType(chargeType);
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/charge-config/{id}")
    public ResponseEntity<ChargeConfiguration> updateChargeConfiguration(
            @PathVariable Long id,
            @RequestBody ChargeConfiguration config) {
        // In a real implementation, we would update the existing record
        ChargeConfiguration updatedConfig = configurationService.saveChargeConfiguration(config);
        return ResponseEntity.ok(updatedConfig);
    }
    
    @DeleteMapping("/charge-config/{id}")
    public ResponseEntity<Void> deleteChargeConfiguration(@PathVariable Long id) {
        // In a real implementation, we would delete the record
        return ResponseEntity.ok().build();
    }
    
    // Transaction Rule Configuration Endpoints
    
    @PostMapping("/rule-config")
    public ResponseEntity<TransactionRuleConfiguration> createRuleConfig(
            @RequestBody TransactionRuleConfiguration config) {
        TransactionRuleConfiguration savedConfig = configurationService.saveTransactionRuleConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/rule-config/{ruleName}")
    public ResponseEntity<TransactionRuleConfiguration> getRuleConfig(
            @PathVariable String ruleName) {
        TransactionRuleConfiguration config = configurationService.getTransactionRuleConfiguration(ruleName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/rule-config")
    public ResponseEntity<List<TransactionRuleConfiguration>> getAllRuleConfigs() {
        // In a real implementation, we would get all rule configurations
        List<TransactionRuleConfiguration> configs = configurationService.getActiveRulesByType("LIMIT");
        return ResponseEntity.ok(configs);
    }
    
    @GetMapping("/rule-config/type/{ruleType}")
    public ResponseEntity<List<TransactionRuleConfiguration>> getRuleConfigsByType(
            @PathVariable String ruleType) {
        List<TransactionRuleConfiguration> configs = configurationService.getActiveRulesByType(ruleType);
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/rule-config/{id}")
    public ResponseEntity<TransactionRuleConfiguration> updateTransactionRuleConfiguration(
            @PathVariable Long id,
            @RequestBody TransactionRuleConfiguration config) {
        // In a real implementation, we would update the existing record
        TransactionRuleConfiguration updatedConfig = configurationService.saveTransactionRuleConfiguration(config);
        return ResponseEntity.ok(updatedConfig);
    }
    
    @DeleteMapping("/rule-config/{id}")
    public ResponseEntity<Void> deleteTransactionRuleConfiguration(@PathVariable Long id) {
        // In a real implementation, we would delete the record
        return ResponseEntity.ok().build();
    }
}