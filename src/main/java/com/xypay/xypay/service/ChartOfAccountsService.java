package com.xypay.xypay.service;

import com.xypay.xypay.domain.ChartOfAccounts;
import com.xypay.xypay.domain.Bank;
import com.xypay.xypay.repository.ChartOfAccountsRepository;
import com.xypay.xypay.repository.BankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ChartOfAccountsService {
    
    @Autowired
    private ChartOfAccountsRepository chartOfAccountsRepository;
    
    @Autowired
    private BankRepository bankRepository;
    
    public ChartOfAccounts createAccount(UUID bankId, String accountCode, String accountName,
                                       ChartOfAccounts.AccountType accountType,
                                       ChartOfAccounts.AccountCategory accountCategory,
                                       String currencyCode) {
        Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        ChartOfAccounts account = new ChartOfAccounts();
        account.setBank(bank);
        account.setAccountCode(accountCode);
        account.setAccountName(accountName);
        account.setAccountType(accountType);
        account.setAccountCategory(accountCategory);
        account.setCurrencyCode(currencyCode);
        account.setIsActive(true);
        account.setAllowPosting(true);
        account.setAccountLevel(1);
        
        // Set normal balance based on account type
        switch (accountType) {
            case ASSET:
            case EXPENSE:
                account.setNormalBalance(ChartOfAccounts.NormalBalance.DEBIT);
                break;
            case LIABILITY:
            case EQUITY:
            case INCOME:
                account.setNormalBalance(ChartOfAccounts.NormalBalance.CREDIT);
                break;
        }
        
        return chartOfAccountsRepository.save(account);
    }
    
    public ChartOfAccounts createSubAccount(UUID bankId, String parentAccountCode, 
                                          String accountCode, String accountName,
                                          ChartOfAccounts.AccountCategory accountCategory,
                                          String currencyCode) {
        // Note: Repository expects Long but we have UUID - using alternative approach
        List<ChartOfAccounts> allAccounts = chartOfAccountsRepository.findAll();
        ChartOfAccounts parentAccount = allAccounts.stream()
            .filter(acc -> acc.getBank() != null && acc.getBank().getId().equals(bankId) && 
                           parentAccountCode.equals(acc.getAccountCode()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Parent account not found"));
        
        ChartOfAccounts subAccount = new ChartOfAccounts();
        subAccount.setBank(parentAccount.getBank());
        subAccount.setAccountCode(accountCode);
        subAccount.setAccountName(accountName);
        subAccount.setAccountType(parentAccount.getAccountType());
        subAccount.setAccountCategory(accountCategory);
        subAccount.setParentAccountCode(parentAccountCode);
        subAccount.setAccountLevel(parentAccount.getAccountLevel() + 1);
        subAccount.setCurrencyCode(currencyCode);
        subAccount.setNormalBalance(parentAccount.getNormalBalance());
        subAccount.setIsActive(true);
        subAccount.setAllowPosting(true);
        
        return chartOfAccountsRepository.save(subAccount);
    }
    
    public List<ChartOfAccounts> getAccountsByBank(UUID bankId) {
        // Note: Repository expects Long but we have UUID - using alternative approach
        return chartOfAccountsRepository.findAll().stream()
            .filter(acc -> acc.getBank() != null && acc.getBank().getId().equals(bankId) && acc.getIsActive())
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<ChartOfAccounts> getAccountsByType(UUID bankId, ChartOfAccounts.AccountType accountType) {
        // Note: Repository expects Long but we have UUID - using alternative approach
        return chartOfAccountsRepository.findAll().stream()
            .filter(acc -> acc.getBank() != null && acc.getBank().getId().equals(bankId) && 
                           accountType.equals(acc.getAccountType()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<ChartOfAccounts> getAccountsByCategory(UUID bankId, ChartOfAccounts.AccountCategory accountCategory) {
        // Note: Repository expects Long but we have UUID - using alternative approach
        return chartOfAccountsRepository.findAll().stream()
            .filter(acc -> acc.getBank() != null && acc.getBank().getId().equals(bankId) && 
                           accountCategory.equals(acc.getAccountCategory()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<ChartOfAccounts> getControlAccounts(UUID bankId) {
        // Note: Repository expects Long but we have UUID - using alternative approach
        return chartOfAccountsRepository.findAll().stream()
            .filter(acc -> acc.getBank() != null && acc.getBank().getId().equals(bankId) && acc.getIsControlAccount())
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<ChartOfAccounts> getPostingAccounts(UUID bankId) {
        // Note: Repository expects Long but we have UUID - using alternative approach
        return chartOfAccountsRepository.findAll().stream()
            .filter(acc -> acc.getBank() != null && acc.getBank().getId().equals(bankId) && acc.getAllowPosting())
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<ChartOfAccounts> getRootAccounts(UUID bankId) {
        // Note: Repository expects Long but we have UUID - using alternative approach
        return chartOfAccountsRepository.findAll().stream()
            .filter(acc -> acc.getBank() != null && acc.getBank().getId().equals(bankId) && 
                           acc.getParentAccountCode() == null)
            .collect(java.util.stream.Collectors.toList());
    }
    
    public List<ChartOfAccounts> getSubAccounts(UUID bankId, String parentAccountCode) {
        // Note: Repository expects Long but we have UUID - using alternative approach
        return chartOfAccountsRepository.findAll().stream()
            .filter(acc -> acc.getBank() != null && acc.getBank().getId().equals(bankId) && 
                           parentAccountCode.equals(acc.getParentAccountCode()))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public Optional<ChartOfAccounts> getAccountByCode(UUID bankId, String accountCode) {
        // Note: Repository expects Long but we have UUID - using alternative approach
        return chartOfAccountsRepository.findAll().stream()
            .filter(acc -> acc.getBank() != null && acc.getBank().getId().equals(bankId) && 
                           accountCode.equals(acc.getAccountCode()))
            .findFirst();
    }
    
    public List<ChartOfAccounts> searchAccounts(UUID bankId, String searchTerm) {
        // Note: Repository expects Long but we have UUID - using alternative approach
        return chartOfAccountsRepository.findAll().stream()
            .filter(acc -> acc.getBank() != null && acc.getBank().getId().equals(bankId) && 
                           (acc.getAccountCode().contains(searchTerm) || acc.getAccountName().contains(searchTerm)))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public ChartOfAccounts updateAccountBalance(UUID accountId, BigDecimal amount, boolean isDebit) {
        ChartOfAccounts account = chartOfAccountsRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.updateBalance(amount, isDebit);
        return chartOfAccountsRepository.save(account);
    }
    
    public void setControlAccount(UUID accountId, boolean isControlAccount) {
        ChartOfAccounts account = chartOfAccountsRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.setIsControlAccount(isControlAccount);
        if (isControlAccount) {
            account.setAllowPosting(false); // Control accounts typically don't allow direct posting
        }
        
        chartOfAccountsRepository.save(account);
    }
    
    public void setPostingPermission(UUID accountId, boolean allowPosting) {
        ChartOfAccounts account = chartOfAccountsRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.setAllowPosting(allowPosting);
        chartOfAccountsRepository.save(account);
    }
    
    public void deactivateAccount(UUID accountId) {
        ChartOfAccounts account = chartOfAccountsRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // Check if account has balance
        if (account.getCurrentBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot deactivate account with non-zero balance");
        }
        
        account.setIsActive(false);
        chartOfAccountsRepository.save(account);
    }
    
    public ChartOfAccounts updateAccountDetails(UUID accountId, String accountName, 
                                              String accountDescription, String regulatoryCode) {
        ChartOfAccounts account = chartOfAccountsRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (accountName != null) account.setAccountName(accountName);
        if (accountDescription != null) account.setAccountDescription(accountDescription);
        if (regulatoryCode != null) account.setRegulatoryCode(regulatoryCode);
        
        return chartOfAccountsRepository.save(account);
    }
    
    public void setBudgetAmount(UUID accountId, BigDecimal budgetAmount) {
        ChartOfAccounts account = chartOfAccountsRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.setBudgetAmount(budgetAmount);
        chartOfAccountsRepository.save(account);
    }
    
    public void setRiskWeight(UUID accountId, BigDecimal riskWeight) {
        ChartOfAccounts account = chartOfAccountsRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.setRiskWeight(riskWeight);
        chartOfAccountsRepository.save(account);
    }
    
    public List<ChartOfAccounts> getStatutoryReturnsAccounts(UUID bankId) {
        // Note: Repository expects Long but we have UUID - using alternative approach
        return chartOfAccountsRepository.findAll().stream()
            .filter(acc -> acc.getBank() != null && acc.getBank().getId().equals(bankId) && acc.getStatutoryReturns())
            .collect(java.util.stream.Collectors.toList());
    }
    
    public Long getAccountCountByType(UUID bankId, ChartOfAccounts.AccountType accountType) {
        // Note: Repository expects Long but we have UUID - using alternative approach
        return chartOfAccountsRepository.findAll().stream()
            .filter(acc -> acc.getBank() != null && acc.getBank().getId().equals(bankId) && 
                           accountType.equals(acc.getAccountType()))
            .count();
    }
    
    public List<Integer> getAccountLevels(UUID bankId) {
        // Note: Repository expects Long but we have UUID - using alternative approach
        return chartOfAccountsRepository.findAll().stream()
            .filter(acc -> acc.getBank() != null && acc.getBank().getId().equals(bankId))
            .map(ChartOfAccounts::getAccountLevel)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
    }
    
    // Standard Chart of Accounts Setup for Nigerian Banks
    public void setupStandardChartOfAccounts(UUID bankId) {
        // Note: Bank variable retrieved but not used - keeping for validation
        bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        // Assets
        createAccount(bankId, "1000", "ASSETS", ChartOfAccounts.AccountType.ASSET, ChartOfAccounts.AccountCategory.CASH_AND_BANK, "NGN");
        createSubAccount(bankId, "1000", "1100", "Cash and Bank Balances", ChartOfAccounts.AccountCategory.CASH_AND_BANK, "NGN");
        createSubAccount(bankId, "1000", "1200", "Loans and Advances", ChartOfAccounts.AccountCategory.LOANS_AND_ADVANCES, "NGN");
        createSubAccount(bankId, "1000", "1300", "Investments", ChartOfAccounts.AccountCategory.INVESTMENTS, "NGN");
        createSubAccount(bankId, "1000", "1400", "Fixed Assets", ChartOfAccounts.AccountCategory.FIXED_ASSETS, "NGN");
        
        // Liabilities
        createAccount(bankId, "2000", "LIABILITIES", ChartOfAccounts.AccountType.LIABILITY, ChartOfAccounts.AccountCategory.CUSTOMER_DEPOSITS, "NGN");
        createSubAccount(bankId, "2000", "2100", "Customer Deposits", ChartOfAccounts.AccountCategory.CUSTOMER_DEPOSITS, "NGN");
        createSubAccount(bankId, "2000", "2200", "Borrowings", ChartOfAccounts.AccountCategory.BORROWINGS, "NGN");
        createSubAccount(bankId, "2000", "2300", "Other Liabilities", ChartOfAccounts.AccountCategory.OTHER_LIABILITIES, "NGN");
        
        // Equity
        createAccount(bankId, "3000", "EQUITY", ChartOfAccounts.AccountType.EQUITY, ChartOfAccounts.AccountCategory.SHARE_CAPITAL, "NGN");
        createSubAccount(bankId, "3000", "3100", "Share Capital", ChartOfAccounts.AccountCategory.SHARE_CAPITAL, "NGN");
        createSubAccount(bankId, "3000", "3200", "Retained Earnings", ChartOfAccounts.AccountCategory.RETAINED_EARNINGS, "NGN");
        
        // Income
        createAccount(bankId, "4000", "INCOME", ChartOfAccounts.AccountType.INCOME, ChartOfAccounts.AccountCategory.INTEREST_INCOME, "NGN");
        createSubAccount(bankId, "4000", "4100", "Interest Income", ChartOfAccounts.AccountCategory.INTEREST_INCOME, "NGN");
        createSubAccount(bankId, "4000", "4200", "Fee Income", ChartOfAccounts.AccountCategory.FEE_INCOME, "NGN");
        
        // Expenses
        createAccount(bankId, "5000", "EXPENSES", ChartOfAccounts.AccountType.EXPENSE, ChartOfAccounts.AccountCategory.INTEREST_EXPENSE, "NGN");
        createSubAccount(bankId, "5000", "5100", "Interest Expense", ChartOfAccounts.AccountCategory.INTEREST_EXPENSE, "NGN");
        createSubAccount(bankId, "5000", "5200", "Operating Expenses", ChartOfAccounts.AccountCategory.OPERATING_EXPENSES, "NGN");
    }
}
