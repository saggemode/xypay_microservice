package com.xypay.xypay.admin;

import com.xypay.xypay.config.AccountConfiguration;
import com.xypay.xypay.config.CustomerConfiguration;
import com.xypay.xypay.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/customer-account")
public class CustomerAccountAdminController {
    
    @Autowired
    private ConfigurationService configurationService;
    
    // Customer Configuration Endpoints
    
    @PostMapping("/customer-config")
    public ResponseEntity<CustomerConfiguration> createCustomerConfiguration(
            @RequestBody CustomerConfiguration config) {
        CustomerConfiguration savedConfig = configurationService.saveCustomerConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/customer-config/{customerType}")
    public ResponseEntity<CustomerConfiguration> getCustomerConfiguration(
            @PathVariable String customerType) {
        CustomerConfiguration config = configurationService.getCustomerConfiguration(customerType);
        return ResponseEntity.ok(config);
    }
    
    @PutMapping("/customer-config/{id}")
    public ResponseEntity<CustomerConfiguration> updateCustomerConfiguration(
            @PathVariable Long id,
            @RequestBody CustomerConfiguration config) {
        // In a real implementation, we would update the existing record
        CustomerConfiguration updatedConfig = configurationService.saveCustomerConfiguration(config);
        return ResponseEntity.ok(updatedConfig);
    }
    
    @DeleteMapping("/customer-config/{id}")
    public ResponseEntity<Void> deleteCustomerConfiguration(@PathVariable Long id) {
        // In a real implementation, we would delete the record
        return ResponseEntity.ok().build();
    }
    
    // Account Configuration Endpoints
    
    @PostMapping("/account-config")
    public ResponseEntity<AccountConfiguration> createAccountConfiguration(
            @RequestBody AccountConfiguration config) {
        AccountConfiguration savedConfig = configurationService.saveAccountConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/account-config/{accountType}")
    public ResponseEntity<AccountConfiguration> getAccountConfiguration(
            @PathVariable String accountType) {
        AccountConfiguration config = configurationService.getAccountConfiguration(accountType);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/account-config")
    public ResponseEntity<List<AccountConfiguration>> getAllAccountConfigurations() {
        List<AccountConfiguration> configs = configurationService.getAllActiveAccountConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/account-config/{id}")
    public ResponseEntity<AccountConfiguration> updateAccountConfiguration(
            @PathVariable Long id,
            @RequestBody AccountConfiguration config) {
        // In a real implementation, we would update the existing record
        AccountConfiguration updatedConfig = configurationService.saveAccountConfiguration(config);
        return ResponseEntity.ok(updatedConfig);
    }
    
    @DeleteMapping("/account-config/{id}")
    public ResponseEntity<Void> deleteAccountConfiguration(@PathVariable Long id) {
        // In a real implementation, we would delete the record
        return ResponseEntity.ok().build();
    }
}