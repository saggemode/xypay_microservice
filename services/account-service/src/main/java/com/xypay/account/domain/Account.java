package com.xypay.account.domain;

import com.xypay.account.enums.AccountStatus;
import com.xypay.account.enums.AccountType;
import com.xypay.account.enums.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_account_customer", columnList = "customer_id"),
    @Index(name = "idx_account_number", columnList = "account_number", unique = true),
    @Index(name = "idx_account_status", columnList = "status"),
    @Index(name = "idx_account_type", columnList = "account_type"),
    @Index(name = "idx_account_branch", columnList = "branch_id")
})
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    @NotNull
    private Long customerId;
    
    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    private String accountNumber;
    
    @Column(name = "account_name", length = 100)
    private String accountName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency = Currency.NGN;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;
    
    @Column(name = "ledger_balance", precision = 19, scale = 2, nullable = false)
    private BigDecimal ledgerBalance = BigDecimal.ZERO;
    
    @Column(name = "available_balance", precision = 19, scale = 2, nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;
    
    @Column(name = "hold_balance", precision = 19, scale = 2, nullable = false)
    private BigDecimal holdBalance = BigDecimal.ZERO;
    
    @Column(name = "overdraft_limit", precision = 19, scale = 2, nullable = false)
    private BigDecimal overdraftLimit = BigDecimal.ZERO;
    
    @Column(name = "overdraft_used", precision = 19, scale = 2, nullable = false)
    private BigDecimal overdraftUsed = BigDecimal.ZERO;
    
    @Column(name = "minimum_balance", precision = 19, scale = 2, nullable = false)
    private BigDecimal minimumBalance = BigDecimal.ZERO;
    
    @Column(name = "interest_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal interestRate = BigDecimal.ZERO;
    
    @Column(name = "branch_id")
    private Long branchId;
    
    @Column(name = "branch_name", length = 100)
    private String branchName;
    
    @Column(name = "account_holder_name", length = 100)
    private String accountHolderName;
    
    @Column(name = "account_holder_email", length = 100)
    private String accountHolderEmail;
    
    @Column(name = "account_holder_phone", length = 20)
    private String accountHolderPhone;
    
    @Column(name = "enable_notifications", nullable = false)
    private boolean enableNotifications = true;
    
    @Column(name = "enable_sms_alerts", nullable = false)
    private boolean enableSMSAlerts = true;
    
    @Column(name = "enable_email_alerts", nullable = false)
    private boolean enableEmailAlerts = true;
    
    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;
    
    @Column(name = "last_interest_calculation_date")
    private LocalDateTime lastInterestCalculationDate;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Account() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Account(Long customerId, String accountNumber, AccountType accountType) {
        this();
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.minimumBalance = BigDecimal.valueOf(accountType.getMinimumBalance());
        this.interestRate = BigDecimal.valueOf(accountType.getInterestRate());
    }
    
    public Account(Long customerId, String accountNumber, AccountType accountType, Currency currency) {
        this(customerId, accountNumber, accountType);
        this.currency = currency;
    }
    
    // Business Logic Methods
    public boolean canDebit(BigDecimal amount) {
        if (!status.canTransact()) {
            return false;
        }
        
        BigDecimal totalAvailable = availableBalance.add(overdraftLimit.subtract(overdraftUsed));
        return totalAvailable.compareTo(amount) >= 0;
    }
    
    public void debit(BigDecimal amount) {
        if (!canDebit(amount)) {
            throw new IllegalStateException("Insufficient balance or account inactive");
        }
        
        if (availableBalance.compareTo(amount) >= 0) {
            // Sufficient available balance
            this.availableBalance = this.availableBalance.subtract(amount);
        } else {
            // Use overdraft
            BigDecimal overdraftNeeded = amount.subtract(availableBalance);
            this.availableBalance = BigDecimal.ZERO;
            this.overdraftUsed = this.overdraftUsed.add(overdraftNeeded);
        }
        
        this.ledgerBalance = this.ledgerBalance.subtract(amount);
        this.lastTransactionDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void credit(BigDecimal amount) {
        if (!status.canTransact()) {
            throw new IllegalStateException("Account is inactive");
        }
        
        // First pay off any overdraft
        if (overdraftUsed.compareTo(BigDecimal.ZERO) > 0) {
            if (amount.compareTo(overdraftUsed) >= 0) {
                // Amount covers all overdraft
                amount = amount.subtract(overdraftUsed);
                this.overdraftUsed = BigDecimal.ZERO;
            } else {
                // Amount partially covers overdraft
                this.overdraftUsed = this.overdraftUsed.subtract(amount);
                amount = BigDecimal.ZERO;
            }
        }
        
        // Add remaining amount to available balance
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.availableBalance = this.availableBalance.add(amount);
        }
        
        this.ledgerBalance = this.ledgerBalance.add(amount);
        this.lastTransactionDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void holdAmount(BigDecimal amount) {
        if (!canDebit(amount)) {
            throw new IllegalStateException("Insufficient balance for hold");
        }
        this.availableBalance = this.availableBalance.subtract(amount);
        this.holdBalance = this.holdBalance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void releaseHold(BigDecimal amount) {
        if (this.holdBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient hold balance");
        }
        this.holdBalance = this.holdBalance.subtract(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void freeze() {
        this.status = AccountStatus.FROZEN;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void unfreeze() {
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void close() {
        this.status = AccountStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void suspend() {
        this.status = AccountStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void activate() {
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setDormant() {
        this.status = AccountStatus.DORMANT;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isOverdraftAvailable() {
        return overdraftLimit.compareTo(BigDecimal.ZERO) > 0 && 
               overdraftUsed.compareTo(overdraftLimit) < 0;
    }
    
    public BigDecimal getOverdraftAvailable() {
        return overdraftLimit.subtract(overdraftUsed);
    }
    
    public boolean isInterestBearing() {
        return interestRate.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean meetsMinimumBalance() {
        return ledgerBalance.compareTo(minimumBalance) >= 0;
    }
    
    public BigDecimal calculateInterest(BigDecimal principal, int days) {
        if (!isInterestBearing()) {
            return BigDecimal.ZERO;
        }
        
        // Simple interest calculation: P * R * T / 365
        BigDecimal rate = interestRate.divide(BigDecimal.valueOf(100));
        BigDecimal time = BigDecimal.valueOf(days).divide(BigDecimal.valueOf(365));
        return principal.multiply(rate).multiply(time);
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    
    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    
    public BigDecimal getLedgerBalance() { return ledgerBalance; }
    public void setLedgerBalance(BigDecimal ledgerBalance) { this.ledgerBalance = ledgerBalance; }
    
    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }
    
    public BigDecimal getHoldBalance() { return holdBalance; }
    public void setHoldBalance(BigDecimal holdBalance) { this.holdBalance = holdBalance; }
    
    public BigDecimal getOverdraftLimit() { return overdraftLimit; }
    public void setOverdraftLimit(BigDecimal overdraftLimit) { this.overdraftLimit = overdraftLimit; }
    
    public BigDecimal getOverdraftUsed() { return overdraftUsed; }
    public void setOverdraftUsed(BigDecimal overdraftUsed) { this.overdraftUsed = overdraftUsed; }
    
    public BigDecimal getMinimumBalance() { return minimumBalance; }
    public void setMinimumBalance(BigDecimal minimumBalance) { this.minimumBalance = minimumBalance; }
    
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    
    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
    
    public String getAccountHolderEmail() { return accountHolderEmail; }
    public void setAccountHolderEmail(String accountHolderEmail) { this.accountHolderEmail = accountHolderEmail; }
    
    public String getAccountHolderPhone() { return accountHolderPhone; }
    public void setAccountHolderPhone(String accountHolderPhone) { this.accountHolderPhone = accountHolderPhone; }
    
    public boolean isEnableNotifications() { return enableNotifications; }
    public void setEnableNotifications(boolean enableNotifications) { this.enableNotifications = enableNotifications; }
    
    public boolean isEnableSMSAlerts() { return enableSMSAlerts; }
    public void setEnableSMSAlerts(boolean enableSMSAlerts) { this.enableSMSAlerts = enableSMSAlerts; }
    
    public boolean isEnableEmailAlerts() { return enableEmailAlerts; }
    public void setEnableEmailAlerts(boolean enableEmailAlerts) { this.enableEmailAlerts = enableEmailAlerts; }
    
    public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
    public void setLastTransactionDate(LocalDateTime lastTransactionDate) { this.lastTransactionDate = lastTransactionDate; }
    
    public LocalDateTime getLastInterestCalculationDate() { return lastInterestCalculationDate; }
    public void setLastInterestCalculationDate(LocalDateTime lastInterestCalculationDate) { this.lastInterestCalculationDate = lastInterestCalculationDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
