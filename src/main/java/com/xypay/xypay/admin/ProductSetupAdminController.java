package com.xypay.xypay.admin;

import com.xypay.xypay.config.AccountConfiguration;
import com.xypay.xypay.config.LoanProductConfiguration;
import com.xypay.xypay.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product-setup")
public class ProductSetupAdminController {
    
    @Autowired
    private ConfigurationService configurationService;
    
    // Deposit Product Configuration Endpoints
    
    @PostMapping("/deposit-config")
    public ResponseEntity<AccountConfiguration> createDepositProduct(
            @RequestBody AccountConfiguration config) {
        AccountConfiguration savedConfig = configurationService.saveAccountConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/deposit-config/{accountType}")
    public ResponseEntity<AccountConfiguration> getDepositProduct(
            @PathVariable String accountType) {
        AccountConfiguration config = configurationService.getAccountConfiguration(accountType);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/deposit-config")
    public ResponseEntity<List<AccountConfiguration>> getAllDepositProducts() {
        List<AccountConfiguration> configs = configurationService.getAllActiveAccountConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/deposit-config/{id}")
    public ResponseEntity<AccountConfiguration> updateDepositProduct(
            @PathVariable Long id,
            @RequestBody AccountConfiguration config) {
        // In a real implementation, we would update the existing record
        AccountConfiguration updatedConfig = configurationService.saveAccountConfiguration(config);
        return ResponseEntity.ok(updatedConfig);
    }
    
    @DeleteMapping("/deposit-config/{id}")
    public ResponseEntity<Void> deleteDepositProduct(@PathVariable Long id) {
        // In a real implementation, we would delete the record
        return ResponseEntity.ok().build();
    }
    
    // Loan Product Configuration Endpoints
    
    @PostMapping("/loan-config")
    public ResponseEntity<LoanProductConfiguration> createLoanProduct(
            @RequestBody LoanProductConfiguration config) {
        LoanProductConfiguration savedConfig = configurationService.saveLoanProductConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/loan-config/{productName}")
    public ResponseEntity<LoanProductConfiguration> getLoanProduct(
            @PathVariable String productName) {
        LoanProductConfiguration config = configurationService.getLoanProductConfiguration(productName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/loan-config")
    public ResponseEntity<List<LoanProductConfiguration>> getAllLoanProducts() {
        List<LoanProductConfiguration> configs = configurationService.getAllActiveLoanProductConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    @PutMapping("/loan-config/{id}")
    public ResponseEntity<LoanProductConfiguration> updateLoanProduct(
            @PathVariable Long id,
            @RequestBody LoanProductConfiguration config) {
        // In a real implementation, we would update the existing record
        LoanProductConfiguration updatedConfig = configurationService.saveLoanProductConfiguration(config);
        return ResponseEntity.ok(updatedConfig);
    }

    @DeleteMapping("/loan-config/{id}")
    public ResponseEntity<Void> deleteLoanProduct(@PathVariable Long id) {
        // In a real implementation, we would delete the record
        return ResponseEntity.ok().build();
    }
}