package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chart_of_accounts")
public class ChartOfAccounts extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;
    
    @Column(name = "account_code", length = 20, unique = true, nullable = false)
    private String accountCode;
    
    @Column(name = "account_name", length = 200, nullable = false)
    private String accountName;
    
    @Column(name = "account_description", length = 500)
    private String accountDescription;
    
    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;
    
    @Column(name = "account_category")
    @Enumerated(EnumType.STRING)
    private AccountCategory accountCategory;
    
    @Column(name = "parent_account_code", length = 20)
    private String parentAccountCode;
    
    @Column(name = "account_level")
    private Integer accountLevel = 1;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_control_account")
    private Boolean isControlAccount = false;
    
    @Column(name = "allow_posting")
    private Boolean allowPosting = true;
    
    @Column(name = "currency_code", length = 3)
    private String currencyCode;
    
    @Column(name = "normal_balance")
    @Enumerated(EnumType.STRING)
    private NormalBalance normalBalance;
    
    @Column(name = "opening_balance", precision = 19, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;
    
    @Column(name = "current_balance", precision = 19, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;
    
    @Column(name = "budget_amount", precision = 19, scale = 2)
    private BigDecimal budgetAmount = BigDecimal.ZERO;
    
    // Basel III and IFRS Classification
    @Column(name = "basel_category", length = 50)
    private String baselCategory;
    
    @Column(name = "ifrs_classification", length = 50)
    private String ifrsClassification;
    
    @Column(name = "risk_weight", precision = 5, scale = 2)
    private BigDecimal riskWeight = BigDecimal.ZERO;
    
    // Regulatory Reporting
    @Column(name = "regulatory_code", length = 20)
    private String regulatoryCode;
    
    @Column(name = "cbn_code", length = 20)
    private String cbnCode; // Central Bank of Nigeria code
    
    @Column(name = "statutory_returns")
    private Boolean statutoryReturns = false;
    
    // Tax Configuration
    @Column(name = "tax_applicable")
    private Boolean taxApplicable = false;
    
    @Column(name = "tax_rate", precision = 5, scale = 4)
    private BigDecimal taxRate = BigDecimal.ZERO;
    
    // Audit and Control
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "approved_by", length = 100)
    private String approvedBy;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column(name = "last_modified_by", length = 100)
    private String lastModifiedBy;
    
    // Relationships
    @OneToMany(mappedBy = "chartOfAccounts", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GeneralLedger> ledgerEntries;
    
    public enum AccountType {
        ASSET, LIABILITY, EQUITY, INCOME, EXPENSE
    }
    
    public enum AccountCategory {
        // Assets
        CASH_AND_BANK, INVESTMENTS, LOANS_AND_ADVANCES, FIXED_ASSETS, 
        OTHER_ASSETS, INTER_BRANCH_ACCOUNTS,
        
        // Liabilities
        CUSTOMER_DEPOSITS, BORROWINGS, ACCRUED_EXPENSES, OTHER_LIABILITIES,
        PROVISIONS, DEFERRED_INCOME,
        
        // Equity
        SHARE_CAPITAL, RETAINED_EARNINGS, RESERVES, ACCUMULATED_LOSSES,
        
        // Income
        INTEREST_INCOME, FEE_INCOME, TRADING_INCOME, OTHER_INCOME,
        
        // Expenses
        INTEREST_EXPENSE, OPERATING_EXPENSES, PERSONNEL_COSTS, 
        DEPRECIATION, PROVISIONS_EXPENSE, OTHER_EXPENSES
    }
    
    public enum NormalBalance {
        DEBIT, CREDIT
    }
    
    // Helper methods
    public boolean isAsset() {
        return accountType == AccountType.ASSET;
    }
    
    public boolean isLiability() {
        return accountType == AccountType.LIABILITY;
    }
    
    public boolean isEquity() {
        return accountType == AccountType.EQUITY;
    }
    
    public boolean isIncome() {
        return accountType == AccountType.INCOME;
    }
    
    public boolean isExpense() {
        return accountType == AccountType.EXPENSE;
    }
    
    public String getFullAccountCode() {
        return bank.getCode() + "-" + accountCode;
    }
    
    public void updateBalance(BigDecimal amount, boolean isDebit) {
        if (normalBalance == NormalBalance.DEBIT) {
            currentBalance = isDebit ? 
                currentBalance.add(amount) : 
                currentBalance.subtract(amount);
        } else {
            currentBalance = isDebit ? 
                currentBalance.subtract(amount) : 
                currentBalance.add(amount);
        }
    }
}
