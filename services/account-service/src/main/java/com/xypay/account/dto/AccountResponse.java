package com.xypay.account.dto;

import com.xypay.account.enums.AccountStatus;
import com.xypay.account.enums.AccountType;
import com.xypay.account.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountResponse {
    
    private Long id;
    private Long customerId;
    private String accountNumber;
    private String accountName;
    private AccountType accountType;
    private Currency currency;
    private AccountStatus status;
    private BigDecimal ledgerBalance;
    private BigDecimal availableBalance;
    private BigDecimal holdBalance;
    private BigDecimal overdraftLimit;
    private BigDecimal overdraftUsed;
    private BigDecimal minimumBalance;
    private BigDecimal interestRate;
    private Long branchId;
    private String branchName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastTransactionDate;
    private boolean isInterestBearing;
    private boolean isOverdraftEnabled;
    private String accountHolderName;
    private String accountHolderEmail;
    private String accountHolderPhone;
    private boolean enableNotifications;
    private boolean enableSMSAlerts;
    private boolean enableEmailAlerts;
    
    // Constructors
    public AccountResponse() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getAccountName() {
        return accountName;
    }
    
    public void setAccountName(String accountName) {
        this.accountName = accountName;
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
    
    public AccountStatus getStatus() {
        return status;
    }
    
    public void setStatus(AccountStatus status) {
        this.status = status;
    }
    
    public BigDecimal getLedgerBalance() {
        return ledgerBalance;
    }
    
    public void setLedgerBalance(BigDecimal ledgerBalance) {
        this.ledgerBalance = ledgerBalance;
    }
    
    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }
    
    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }
    
    public BigDecimal getHoldBalance() {
        return holdBalance;
    }
    
    public void setHoldBalance(BigDecimal holdBalance) {
        this.holdBalance = holdBalance;
    }
    
    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }
    
    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }
    
    public BigDecimal getOverdraftUsed() {
        return overdraftUsed;
    }
    
    public void setOverdraftUsed(BigDecimal overdraftUsed) {
        this.overdraftUsed = overdraftUsed;
    }
    
    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }
    
    public void setMinimumBalance(BigDecimal minimumBalance) {
        this.minimumBalance = minimumBalance;
    }
    
    public BigDecimal getInterestRate() {
        return interestRate;
    }
    
    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }
    
    public Long getBranchId() {
        return branchId;
    }
    
    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }
    
    public String getBranchName() {
        return branchName;
    }
    
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getLastTransactionDate() {
        return lastTransactionDate;
    }
    
    public void setLastTransactionDate(LocalDateTime lastTransactionDate) {
        this.lastTransactionDate = lastTransactionDate;
    }
    
    public boolean isInterestBearing() {
        return isInterestBearing;
    }
    
    public void setInterestBearing(boolean interestBearing) {
        isInterestBearing = interestBearing;
    }
    
    public boolean isOverdraftEnabled() {
        return isOverdraftEnabled;
    }
    
    public void setOverdraftEnabled(boolean overdraftEnabled) {
        isOverdraftEnabled = overdraftEnabled;
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
