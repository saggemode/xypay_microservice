package com.xypay.account.dto;

import com.xypay.account.enums.AccountType;
import com.xypay.account.enums.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class AccountCreationRequest {
    
    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    private Long customerId;
    
    @NotNull(message = "Account type is required")
    private AccountType accountType;
    
    @NotNull(message = "Currency is required")
    private Currency currency;
    
    @NotNull(message = "Initial deposit is required")
    @Positive(message = "Initial deposit must be positive")
    private BigDecimal initialDeposit;
    
    private Long branchId;
    
    @Size(max = 100, message = "Account description cannot exceed 100 characters")
    private String description;
    
    private String accountHolderName;
    
    private String accountHolderEmail;
    
    private String accountHolderPhone;
    
    private boolean enableOverdraft = false;
    
    private BigDecimal overdraftLimit = BigDecimal.ZERO;
    
    private boolean enableNotifications = true;
    
    private boolean enableSMSAlerts = true;
    
    private boolean enableEmailAlerts = true;
    
    // Constructors
    public AccountCreationRequest() {}
    
    public AccountCreationRequest(Long customerId, AccountType accountType, Currency currency, BigDecimal initialDeposit) {
        this.customerId = customerId;
        this.accountType = accountType;
        this.currency = currency;
        this.initialDeposit = initialDeposit;
    }
    
    // Getters and Setters
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public AccountType getAccountType() {
        return accountType;
    }
    
    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
    
    public BigDecimal getInitialDeposit() {
        return initialDeposit;
    }
    
    public void setInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
    }
    
    public Long getBranchId() {
        return branchId;
    }
    
    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAccountHolderName() {
        return accountHolderName;
    }
    
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }
    
    public String getAccountHolderEmail() {
        return accountHolderEmail;
    }
    
    public void setAccountHolderEmail(String accountHolderEmail) {
        this.accountHolderEmail = accountHolderEmail;
    }
    
    public String getAccountHolderPhone() {
        return accountHolderPhone;
    }
    
    public void setAccountHolderPhone(String accountHolderPhone) {
        this.accountHolderPhone = accountHolderPhone;
    }
    
    public boolean isEnableOverdraft() {
        return enableOverdraft;
    }
    
    public void setEnableOverdraft(boolean enableOverdraft) {
        this.enableOverdraft = enableOverdraft;
    }
    
    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }
    
    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }
    
    public boolean isEnableNotifications() {
        return enableNotifications;
    }
    
    public void setEnableNotifications(boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
    }
    
    public boolean isEnableSMSAlerts() {
        return enableSMSAlerts;
    }
    
    public void setEnableSMSAlerts(boolean enableSMSAlerts) {
        this.enableSMSAlerts = enableSMSAlerts;
    }
    
    public boolean isEnableEmailAlerts() {
        return enableEmailAlerts;
    }
    
    public void setEnableEmailAlerts(boolean enableEmailAlerts) {
        this.enableEmailAlerts = enableEmailAlerts;
    }
}
