package com.xypay.xypay.controller;

import com.xypay.xypay.config.*;
import com.xypay.xypay.domain.AlertNotificationConfiguration;
import com.xypay.xypay.domain.IntegrationConfiguration;
import com.xypay.xypay.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/configurations")
public class ConfigurationController {
    
    @Autowired
    private ConfigurationService configurationService;
    
    // Customer Configuration Endpoints
    @PostMapping("/customer")
    public ResponseEntity<CustomerConfiguration> saveCustomerConfiguration(
            @RequestBody CustomerConfiguration config) {
        CustomerConfiguration savedConfig = configurationService.saveCustomerConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/customer/{customerType}")
    public ResponseEntity<CustomerConfiguration> getCustomerConfiguration(
            @PathVariable String customerType) {
        CustomerConfiguration config = configurationService.getCustomerConfiguration(customerType);
        return ResponseEntity.ok(config);
    }
    
    // Account Configuration Endpoints
    @PostMapping("/account")
    public ResponseEntity<AccountConfiguration> saveAccountConfiguration(
            @RequestBody AccountConfiguration config) {
        AccountConfiguration savedConfig = configurationService.saveAccountConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/account/{accountType}")
    public ResponseEntity<AccountConfiguration> getAccountConfiguration(
            @PathVariable String accountType) {
        AccountConfiguration config = configurationService.getAccountConfiguration(accountType);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/account")
    public ResponseEntity<List<AccountConfiguration>> getAllActiveAccountConfigurations() {
        List<AccountConfiguration> configs = configurationService.getAllActiveAccountConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    // Loan Product Configuration Endpoints
    @PostMapping("/loan-product")
    public ResponseEntity<LoanProductConfiguration> saveLoanProductConfiguration(
            @RequestBody LoanProductConfiguration config) {
        LoanProductConfiguration savedConfig = configurationService.saveLoanProductConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/loan-product/{productName}")
    public ResponseEntity<LoanProductConfiguration> getLoanProductConfiguration(
            @PathVariable String productName) {
        LoanProductConfiguration config = configurationService.getLoanProductConfiguration(productName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/loan-product")
    public ResponseEntity<List<LoanProductConfiguration>> getAllActiveLoanProductConfigurations() {
        List<LoanProductConfiguration> configs = configurationService.getAllActiveLoanProductConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    // Interest Configuration Endpoints
    @PostMapping("/interest")
    public ResponseEntity<InterestConfiguration> saveInterestConfiguration(
            @RequestBody InterestConfiguration config) {
        InterestConfiguration savedConfig = configurationService.saveInterestConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/interest/{configName}")
    public ResponseEntity<InterestConfiguration> getInterestConfiguration(
            @PathVariable String configName) {
        InterestConfiguration config = configurationService.getInterestConfiguration(configName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/interest")
    public ResponseEntity<List<InterestConfiguration>> getAllActiveInterestConfigurations() {
        List<InterestConfiguration> configs = configurationService.getAllActiveInterestConfigurations();
        return ResponseEntity.ok(configs);
    }
    
    // Charge Configuration Endpoints
    @PostMapping("/charge")
    public ResponseEntity<ChargeConfiguration> saveChargeConfiguration(
            @RequestBody ChargeConfiguration config) {
        ChargeConfiguration savedConfig = configurationService.saveChargeConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/charge/{chargeName}")
    public ResponseEntity<ChargeConfiguration> getChargeConfiguration(
            @PathVariable String chargeName) {
        ChargeConfiguration config = configurationService.getChargeConfiguration(chargeName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/charge/type/{chargeType}")
    public ResponseEntity<List<ChargeConfiguration>> getActiveChargesByType(
            @PathVariable String chargeType) {
        List<ChargeConfiguration> configs = configurationService.getActiveChargesByType(chargeType);
        return ResponseEntity.ok(configs);
    }
    
    // Transaction Rule Configuration Endpoints
    @PostMapping("/transaction-rule")
    public ResponseEntity<TransactionRuleConfiguration> saveTransactionRuleConfiguration(
            @RequestBody TransactionRuleConfiguration config) {
        TransactionRuleConfiguration savedConfig = configurationService.saveTransactionRuleConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/transaction-rule/{ruleName}")
    public ResponseEntity<TransactionRuleConfiguration> getTransactionRuleConfiguration(
            @PathVariable String ruleName) {
        TransactionRuleConfiguration config = configurationService.getTransactionRuleConfiguration(ruleName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/transaction-rule/type/{ruleType}")
    public ResponseEntity<List<TransactionRuleConfiguration>> getActiveRulesByType(
            @PathVariable String ruleType) {
        List<TransactionRuleConfiguration> configs = configurationService.getActiveRulesByType(ruleType);
        return ResponseEntity.ok(configs);
    }
    
    // Risk & Compliance Configuration Endpoints
    @PostMapping("/risk-compliance")
    public ResponseEntity<RiskComplianceConfiguration> saveRiskComplianceConfiguration(
            @RequestBody RiskComplianceConfiguration config) {
        RiskComplianceConfiguration savedConfig = configurationService.saveRiskComplianceConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/risk-compliance/{configName}")
    public ResponseEntity<RiskComplianceConfiguration> getRiskComplianceConfiguration(
            @PathVariable String configName) {
        RiskComplianceConfiguration config = configurationService.getRiskComplianceConfiguration(configName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/risk-compliance/type/{configType}")
    public ResponseEntity<List<RiskComplianceConfiguration>> getActiveConfigurationsByType(
            @PathVariable String configType) {
        List<RiskComplianceConfiguration> configs = configurationService.getActiveConfigurationsByType(configType);
        return ResponseEntity.ok(configs);
    }
    
    // Branch & Entity Configuration Endpoints
    @PostMapping("/branch-entity")
    public ResponseEntity<BranchEntityConfiguration> saveBranchEntityConfiguration(
            @RequestBody BranchEntityConfiguration config) {
        BranchEntityConfiguration savedConfig = configurationService.saveBranchEntityConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/branch-entity/{branchCode}")
    public ResponseEntity<BranchEntityConfiguration> getBranchEntityConfiguration(
            @PathVariable String branchCode) {
        BranchEntityConfiguration config = configurationService.getBranchEntityConfiguration(branchCode);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/branch-entity/type/{entityType}")
    public ResponseEntity<List<BranchEntityConfiguration>> getActiveEntitiesByType(
            @PathVariable String entityType) {
        List<BranchEntityConfiguration> configs = configurationService.getActiveEntitiesByType(entityType);
        return ResponseEntity.ok(configs);
    }
    
    // Reporting Configuration Endpoints
    @PostMapping("/reporting")
    public ResponseEntity<ReportingConfiguration> saveReportingConfiguration(
            @RequestBody ReportingConfiguration config) {
        ReportingConfiguration savedConfig = configurationService.saveReportingConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/reporting/{reportName}")
    public ResponseEntity<ReportingConfiguration> getReportingConfiguration(
            @PathVariable String reportName) {
        ReportingConfiguration config = configurationService.getReportingConfiguration(reportName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/reporting/type/{reportType}")
    public ResponseEntity<List<ReportingConfiguration>> getActiveReportsByType(
            @PathVariable String reportType) {
        List<ReportingConfiguration> configs = configurationService.getActiveReportsByType(reportType);
        return ResponseEntity.ok(configs);
    }
    
    // Workflow Configuration Endpoints
    @PostMapping("/workflow")
    public ResponseEntity<WorkflowConfiguration> saveWorkflowConfiguration(
            @RequestBody WorkflowConfiguration config) {
        WorkflowConfiguration savedConfig = configurationService.saveWorkflowConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/workflow/{workflowName}")
    public ResponseEntity<WorkflowConfiguration> getWorkflowConfiguration(
            @PathVariable String workflowName) {
        WorkflowConfiguration config = configurationService.getWorkflowConfiguration(workflowName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/workflow/process-type/{processType}")
    public ResponseEntity<List<WorkflowConfiguration>> getActiveWorkflowsByProcessType(
            @PathVariable String processType) {
        List<WorkflowConfiguration> configs = configurationService.getActiveWorkflowsByProcessType(processType);
        return ResponseEntity.ok(configs);
    }
    
    // Alert & Notification Configuration Endpoints
    @PostMapping("/alert-notification")
    public ResponseEntity<AlertNotificationConfiguration> saveAlertNotificationConfiguration(
            @RequestBody AlertNotificationConfiguration config) {
        AlertNotificationConfiguration savedConfig = configurationService.saveAlertNotificationConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/alert-notification/{alertName}")
    public ResponseEntity<AlertNotificationConfiguration> getAlertNotificationConfiguration(
            @PathVariable String alertName) {
        AlertNotificationConfiguration config = configurationService.getAlertNotificationConfiguration(alertName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/alert-notification/type/{alertType}")
    public ResponseEntity<List<AlertNotificationConfiguration>> getActiveAlertsByType(
            @PathVariable String alertType) {
        List<AlertNotificationConfiguration> configs = configurationService.getActiveAlertsByType(alertType);
        return ResponseEntity.ok(configs);
    }
    
    // Integration Configuration Endpoints
    @PostMapping("/integration")
    public ResponseEntity<IntegrationConfiguration> saveIntegrationConfiguration(
            @RequestBody IntegrationConfiguration config) {
        IntegrationConfiguration savedConfig = configurationService.saveIntegrationConfiguration(config);
        return ResponseEntity.ok(savedConfig);
    }
    
    @GetMapping("/integration/{integrationName}")
    public ResponseEntity<IntegrationConfiguration> getIntegrationConfiguration(
            @PathVariable String integrationName) {
        IntegrationConfiguration config = configurationService.getIntegrationConfiguration(integrationName);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/integration/type/{integrationType}")
    public ResponseEntity<List<IntegrationConfiguration>> getActiveIntegrationsByType(
            @PathVariable String integrationType) {
        List<IntegrationConfiguration> configs = configurationService.getActiveIntegrationsByType(integrationType);
        return ResponseEntity.ok(configs);
    }
}