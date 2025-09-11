package com.xypay.xypay.service;

import com.xypay.xypay.config.*;
import com.xypay.xypay.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConfigurationService {
    
    @Autowired
    private CustomerConfigurationRepository customerConfigRepo;
    
    @Autowired
    private AccountConfigurationRepository accountConfigRepo;
    
    @Autowired
    private LoanProductConfigurationRepository loanProductConfigRepo;
    
    @Autowired
    private InterestConfigurationRepository interestConfigRepo;
    
    @Autowired
    private ChargeConfigurationRepository chargeConfigRepo;
    
    @Autowired
    private TransactionRuleConfigurationRepository transactionRuleConfigRepo;
    
    @Autowired
    private RiskComplianceConfigurationRepository riskComplianceConfigRepo;
    
    @Autowired
    private BranchEntityConfigurationRepository branchEntityConfigRepo;
    
    @Autowired
    private ReportingConfigurationRepository reportingConfigRepo;
    
    @Autowired
    private WorkflowConfigurationRepository workflowConfigRepo;
    
    @Autowired
    private com.xypay.xypay.repository.AlertNotificationConfigurationRepository alertNotificationConfigRepo;
    
    @Autowired
    private com.xypay.xypay.repository.IntegrationConfigurationRepository integrationConfigRepo;
    
    // Customer Configuration Methods
    public CustomerConfiguration saveCustomerConfiguration(CustomerConfiguration config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return customerConfigRepo.save(config);
    }
    
    public CustomerConfiguration getCustomerConfiguration(String customerType) {
        return customerConfigRepo.findByCustomerType(customerType)
                .orElseThrow(() -> new RuntimeException("Customer configuration not found for type: " + customerType));
    }
    
    // Account Configuration Methods
    public AccountConfiguration saveAccountConfiguration(AccountConfiguration config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return accountConfigRepo.save(config);
    }
    
    public AccountConfiguration getAccountConfiguration(String accountType) {
        return accountConfigRepo.findByAccountType(accountType)
                .orElseThrow(() -> new RuntimeException("Account configuration not found for type: " + accountType));
    }
    
    public List<AccountConfiguration> getAllActiveAccountConfigurations() {
        return accountConfigRepo.findByIsActiveTrue();
    }
    
    // Loan Product Configuration Methods
    public LoanProductConfiguration saveLoanProductConfiguration(LoanProductConfiguration config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return loanProductConfigRepo.save(config);
    }
    
    public LoanProductConfiguration getLoanProductConfiguration(String productName) {
        return loanProductConfigRepo.findByProductName(productName)
                .orElseThrow(() -> new RuntimeException("Loan product configuration not found for name: " + productName));
    }
    
    public List<LoanProductConfiguration> getAllActiveLoanProductConfigurations() {
        return loanProductConfigRepo.findByIsActiveTrue();
    }
    
    // Interest Configuration Methods
    public InterestConfiguration saveInterestConfiguration(InterestConfiguration config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return interestConfigRepo.save(config);
    }
    
    public InterestConfiguration getInterestConfiguration(String configName) {
        return interestConfigRepo.findByConfigurationName(configName)
                .orElseThrow(() -> new RuntimeException("Interest configuration not found for name: " + configName));
    }
    
    public List<InterestConfiguration> getAllActiveInterestConfigurations() {
        return interestConfigRepo.findByIsActiveTrue();
    }
    
    // Charge Configuration Methods
    public ChargeConfiguration saveChargeConfiguration(ChargeConfiguration config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return chargeConfigRepo.save(config);
    }
    
    public ChargeConfiguration getChargeConfiguration(String chargeName) {
        return chargeConfigRepo.findByChargeName(chargeName)
                .orElseThrow(() -> new RuntimeException("Charge configuration not found for name: " + chargeName));
    }
    
    public List<ChargeConfiguration> getActiveChargesByType(String chargeType) {
        return chargeConfigRepo.findByChargeTypeAndIsActiveTrue(chargeType);
    }
    
    // Transaction Rule Configuration Methods
    public TransactionRuleConfiguration saveTransactionRuleConfiguration(TransactionRuleConfiguration config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return transactionRuleConfigRepo.save(config);
    }
    
    public TransactionRuleConfiguration getTransactionRuleConfiguration(String ruleName) {
        return transactionRuleConfigRepo.findByRuleName(ruleName)
                .orElseThrow(() -> new RuntimeException("Transaction rule configuration not found for name: " + ruleName));
    }
    
    public List<TransactionRuleConfiguration> getActiveRulesByType(String ruleType) {
        return transactionRuleConfigRepo.findByRuleTypeAndIsActiveTrue(ruleType);
    }
    
    // Risk & Compliance Configuration Methods
    public RiskComplianceConfiguration saveRiskComplianceConfiguration(RiskComplianceConfiguration config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return riskComplianceConfigRepo.save(config);
    }
    
    public RiskComplianceConfiguration getRiskComplianceConfiguration(String configName) {
        return riskComplianceConfigRepo.findByConfigurationName(configName)
                .orElseThrow(() -> new RuntimeException("Risk/Compliance configuration not found for name: " + configName));
    }
    
    public List<RiskComplianceConfiguration> getActiveConfigurationsByType(String configType) {
        return riskComplianceConfigRepo.findByConfigTypeAndIsActiveTrue(configType);
    }
    
    public List<RiskComplianceConfiguration> getAllActiveRiskComplianceConfigurations() {
        return riskComplianceConfigRepo.findByIsActiveTrue();
    }
    
    // Branch & Entity Configuration Methods
    public BranchEntityConfiguration saveBranchEntityConfiguration(BranchEntityConfiguration config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return branchEntityConfigRepo.save(config);
    }
    
    public BranchEntityConfiguration getBranchEntityConfiguration(String branchCode) {
        return branchEntityConfigRepo.findByBranchCode(branchCode)
                .orElseThrow(() -> new RuntimeException("Branch/Entity configuration not found for code: " + branchCode));
    }
    
    public List<BranchEntityConfiguration> getActiveEntitiesByType(String entityType) {
        return branchEntityConfigRepo.findByEntityTypeAndIsActiveTrue(entityType);
    }
    
    public List<BranchEntityConfiguration> getAllActiveBranchEntityConfigurations() {
        return branchEntityConfigRepo.findByIsActiveTrue();
    }
    
    // Reporting Configuration Methods
    public ReportingConfiguration saveReportingConfiguration(ReportingConfiguration config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return reportingConfigRepo.save(config);
    }
    
    public ReportingConfiguration getReportingConfiguration(String reportName) {
        return reportingConfigRepo.findByReportName(reportName)
                .orElseThrow(() -> new RuntimeException("Reporting configuration not found for name: " + reportName));
    }
    
    public List<ReportingConfiguration> getActiveReportsByType(String reportType) {
        return reportingConfigRepo.findByReportTypeAndIsActiveTrue(reportType);
    }
    
    public List<ReportingConfiguration> getAllActiveReportingConfigurations() {
        return reportingConfigRepo.findByIsActiveTrue();
    }
    
    // Workflow Configuration Methods
    public WorkflowConfiguration saveWorkflowConfiguration(WorkflowConfiguration config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return workflowConfigRepo.save(config);
    }
    
    public WorkflowConfiguration getWorkflowConfiguration(String workflowName) {
        return workflowConfigRepo.findByWorkflowName(workflowName)
                .orElseThrow(() -> new RuntimeException("Workflow configuration not found for name: " + workflowName));
    }
    
    public List<WorkflowConfiguration> getActiveWorkflowsByProcessType(String processType) {
        return workflowConfigRepo.findByProcessTypeAndIsActiveTrue(processType);
    }
    
    public List<WorkflowConfiguration> getAllActiveWorkflowConfigurations() {
        return workflowConfigRepo.findByIsActiveTrue();
    }
    
    // Alert & Notification Configuration Methods
    public com.xypay.xypay.domain.AlertNotificationConfiguration saveAlertNotificationConfiguration(com.xypay.xypay.domain.AlertNotificationConfiguration config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return alertNotificationConfigRepo.save(config);
    }
    
    public com.xypay.xypay.domain.AlertNotificationConfiguration getAlertNotificationConfiguration(String alertName) {
        return alertNotificationConfigRepo.findByAlertName(alertName)
                .orElseThrow(() -> new RuntimeException("Alert/Notification configuration not found for name: " + alertName));
    }
    
    public List<com.xypay.xypay.domain.AlertNotificationConfiguration> getActiveAlertsByType(String alertType) {
        return alertNotificationConfigRepo.findByAlertTypeAndIsActiveTrue(alertType);
    }
    
    public List<com.xypay.xypay.domain.AlertNotificationConfiguration> getAllActiveAlertNotificationConfigurations() {
        return alertNotificationConfigRepo.findByIsActiveTrue();
    }
    
    public com.xypay.xypay.domain.AlertNotificationConfiguration updateAlertNotificationConfiguration(Long id, com.xypay.xypay.domain.AlertNotificationConfiguration config) {
        com.xypay.xypay.domain.AlertNotificationConfiguration existing = alertNotificationConfigRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert/Notification configuration not found for id: " + id));
        config.setId(id);
        config.setCreatedAt(existing.getCreatedAt());
        config.setUpdatedAt(LocalDateTime.now());
        return alertNotificationConfigRepo.save(config);
    }
    
    public void deleteAlertNotificationConfiguration(Long id) {
        alertNotificationConfigRepo.deleteById(id);
    }
    
    // Integration Configuration Methods
    public com.xypay.xypay.domain.IntegrationConfiguration saveIntegrationConfiguration(com.xypay.xypay.domain.IntegrationConfiguration config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        return integrationConfigRepo.save(config);
    }
    
    public com.xypay.xypay.domain.IntegrationConfiguration getIntegrationConfiguration(String integrationName) {
        return integrationConfigRepo.findByIntegrationName(integrationName)
                .orElseThrow(() -> new RuntimeException("Integration configuration not found for name: " + integrationName));
    }
    
    public List<com.xypay.xypay.domain.IntegrationConfiguration> getActiveIntegrationsByType(String integrationType) {
        return integrationConfigRepo.findByIntegrationTypeAndIsActiveTrue(integrationType);
    }
    
    public List<com.xypay.xypay.domain.IntegrationConfiguration> getAllActiveIntegrationConfigurations() {
        return integrationConfigRepo.findByIsActiveTrue();
    }
    
    public com.xypay.xypay.domain.IntegrationConfiguration updateIntegrationConfiguration(Long id, com.xypay.xypay.domain.IntegrationConfiguration config) {
        com.xypay.xypay.domain.IntegrationConfiguration existing = integrationConfigRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Integration configuration not found for id: " + id));
        config.setId(id);
        config.setCreatedAt(existing.getCreatedAt());
        config.setUpdatedAt(LocalDateTime.now());
        return integrationConfigRepo.save(config);
    }
    
    public void deleteIntegrationConfiguration(Long id) {
        integrationConfigRepo.deleteById(id);
    }
}