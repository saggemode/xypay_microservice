package com.xypay.xypay.controller;

import com.xypay.xypay.domain.Account;
import com.xypay.xypay.domain.Customer;
import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.service.ConfigurationUsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/config-demo")
public class ConfigurationUsageController {
    
    @Autowired
    private ConfigurationUsageService configurationUsageService;
    
    @PostMapping("/validate-customer")
    public ResponseEntity<Boolean> validateCustomer(@RequestBody Customer customer) {
        boolean isValid = configurationUsageService.validateCustomerForAccountOpening(customer);
        return ResponseEntity.ok(isValid);
    }
    
    @PostMapping("/validate-account")
    public ResponseEntity<Boolean> validateAccount(
            @RequestBody Account account, 
            @RequestParam BigDecimal initialDeposit) {
        boolean isValid = configurationUsageService.validateAccountOpeningRequirements(account, initialDeposit);
        return ResponseEntity.ok(isValid);
    }
    
    @PostMapping("/calculate-interest")
    public ResponseEntity<BigDecimal> calculateInterest(
            @RequestBody Account account,
            @RequestParam BigDecimal balance,
            @RequestParam int days) {
        BigDecimal interest = configurationUsageService.calculateInterest(account, balance, days);
        return ResponseEntity.ok(interest);
    }
    
    @PostMapping("/apply-charges")
    public ResponseEntity<BigDecimal> applyCharges(@RequestBody Transaction transaction) {
        BigDecimal charges = configurationUsageService.applyTransactionCharges(transaction);
        return ResponseEntity.ok(charges);
    }
    
    @PostMapping("/validate-transaction")
    public ResponseEntity<Boolean> validateTransaction(@RequestBody Transaction transaction) {
        boolean isValid = configurationUsageService.validateTransactionRules(transaction);
        return ResponseEntity.ok(isValid);
    }
    
    @PostMapping("/check-compliance")
    public ResponseEntity<Boolean> checkCompliance(@RequestBody Transaction transaction) {
        boolean isCompliant = configurationUsageService.checkRiskCompliance(transaction);
        return ResponseEntity.ok(isCompliant);
    }
    
    @PostMapping("/generate-account-number")
    public ResponseEntity<String> generateAccountNumber(@RequestParam String branchCode) {
        String accountNumber = configurationUsageService.generateAccountNumber(branchCode);
        return ResponseEntity.ok(accountNumber);
    }
    
    @PostMapping("/schedule-reports")
    public ResponseEntity<String> scheduleReports() {
        configurationUsageService.scheduleReportGeneration();
        return ResponseEntity.ok("Report scheduling initiated");
    }
    
    @PostMapping("/send-alerts")
    public ResponseEntity<String> sendAlerts(
            @RequestParam String alertType,
            @RequestParam String message) {
        configurationUsageService.sendAlertNotifications(alertType, message);
        return ResponseEntity.ok("Alert notifications sent");
    }
}