package com.xypay.account.service;

import com.xypay.account.domain.Account;
import com.xypay.account.domain.AccountLimits;
import com.xypay.account.dto.AccountCreationRequest;
import com.xypay.account.dto.AccountResponse;
import com.xypay.account.enums.AccountStatus;
import com.xypay.account.enums.AccountType;
import com.xypay.account.enums.Currency;
import com.xypay.account.repository.AccountLimitsRepository;
import com.xypay.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CoreBankingService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private AccountLimitsRepository accountLimitsRepository;
    
    // Account Creation and Management
    public AccountResponse createAccount(AccountCreationRequest request) {
        // Validate customer exists (would call customer service)
        // Validate initial deposit meets minimum requirements
        if (request.getInitialDeposit().compareTo(BigDecimal.valueOf(request.getAccountType().getMinimumBalance())) < 0) {
            throw new IllegalArgumentException("Initial deposit is below minimum required amount");
        }
        
        String accountNumber = generateAccountNumber();
        Account account = new Account(
            request.getCustomerId(), 
            accountNumber, 
            request.getAccountType(), 
            request.getCurrency()
        );
        
        // Set additional account details
        account.setAccountName(request.getAccountHolderName());
        account.setAccountHolderName(request.getAccountHolderName());
        account.setAccountHolderEmail(request.getAccountHolderEmail());
        account.setAccountHolderPhone(request.getAccountHolderPhone());
        account.setBranchId(request.getBranchId());
        account.setEnableNotifications(request.isEnableNotifications());
        account.setEnableSMSAlerts(request.isEnableSMSAlerts());
        account.setEnableEmailAlerts(request.isEnableEmailAlerts());
        
        // Set overdraft if enabled
        if (request.isEnableOverdraft()) {
            account.setOverdraftLimit(request.getOverdraftLimit());
        }
        
        // Credit initial deposit
        account.credit(request.getInitialDeposit());
        
        // Save account
        Account savedAccount = accountRepository.save(account);
        
        // Create default account limits
        createDefaultAccountLimits(savedAccount);
        
        // Note: Initial deposit transaction will be created by Transaction Service
        
        return mapToAccountResponse(savedAccount);
    }
    
    // Account Balance Management (for Transaction Service to use)
    public void debitAccount(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // Validate account status
        if (!account.getStatus().canTransact()) {
            throw new IllegalStateException("Account is not in a transactable state");
        }
        
        account.debit(amount);
        accountRepository.save(account);
    }
    
    public void creditAccount(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // Validate account status
        if (!account.getStatus().canTransact()) {
            throw new IllegalStateException("Account is not in a transactable state");
        }
        
        account.credit(amount);
        accountRepository.save(account);
    }
    
    // Balance Management
    public BigDecimal getAccountBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        return account.getAvailableBalance();
    }
    
    public BigDecimal getLedgerBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        return account.getLedgerBalance();
    }
    
    public boolean canDebit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        return account.canDebit(amount);
    }
    
    // Account Status Management
    public AccountResponse freezeAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.freeze();
        Account savedAccount = accountRepository.save(account);
        
        return mapToAccountResponse(savedAccount);
    }
    
    public AccountResponse unfreezeAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.unfreeze();
        Account savedAccount = accountRepository.save(account);
        
        return mapToAccountResponse(savedAccount);
    }
    
    public AccountResponse closeAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (account.getLedgerBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot close account with non-zero balance");
        }
        
        account.close();
        Account savedAccount = accountRepository.save(account);
        
        return mapToAccountResponse(savedAccount);
    }
    
    // Interest Calculation
    public void calculateAndCreditInterest(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.isInterestBearing()) {
            return;
        }
        
        LocalDateTime lastCalculation = account.getLastInterestCalculationDate();
        if (lastCalculation == null) {
            lastCalculation = account.getCreatedAt();
        }
        
        int daysSinceLastCalculation = (int) ChronoUnit.DAYS.between(lastCalculation, LocalDateTime.now());
        
        if (daysSinceLastCalculation > 0) {
            BigDecimal interest = account.calculateInterest(account.getLedgerBalance(), daysSinceLastCalculation);
            
            if (interest.compareTo(BigDecimal.ZERO) > 0) {
                account.credit(interest);
                account.setLastInterestCalculationDate(LocalDateTime.now());
                accountRepository.save(account);
                
                // Note: Interest transaction will be created by Transaction Service
            }
        }
    }
    
    // Account Limits Management
    public void createDefaultAccountLimits(Account account) {
        // Daily debit limit
        createAccountLimit(account, "DAILY_DEBIT", BigDecimal.valueOf(50000), "DAILY");
        
        // Daily credit limit
        createAccountLimit(account, "DAILY_CREDIT", BigDecimal.valueOf(100000), "DAILY");
        
        // Single transaction limit
        createAccountLimit(account, "SINGLE_TRANSACTION", BigDecimal.valueOf(25000), "SINGLE");
        
        // Monthly debit limit
        createAccountLimit(account, "MONTHLY_DEBIT", BigDecimal.valueOf(500000), "MONTHLY");
        
        // ATM daily limit
        createAccountLimit(account, "ATM_DAILY", BigDecimal.valueOf(20000), "DAILY");
    }
    
    public void createAccountLimit(Account account, String limitType, BigDecimal limitAmount, String limitPeriod) {
        AccountLimits limit = new AccountLimits(account.getId(), account.getAccountNumber(), 
                                               limitType, limitAmount, limitPeriod);
        accountLimitsRepository.save(limit);
    }
    
    // Note: Transaction history methods moved to Transaction Service
    
    // Account Information
    public AccountResponse getAccountDetails(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        return mapToAccountResponse(account);
    }
    
    public List<AccountResponse> getAccountsByCustomer(Long customerId) {
        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        return accounts.stream().map(this::mapToAccountResponse).toList();
    }
    
    // Private Helper Methods
    private String generateAccountNumber() {
        // Generate unique account number
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return "10" + timestamp.substring(timestamp.length() - 6) + random;
    }
    
    // Note: Transaction validation and creation methods moved to Transaction Service
    
    private AccountResponse mapToAccountResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setCustomerId(account.getCustomerId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountName(account.getAccountName());
        response.setAccountType(account.getAccountType());
        response.setCurrency(account.getCurrency());
        response.setStatus(account.getStatus());
        response.setLedgerBalance(account.getLedgerBalance());
        response.setAvailableBalance(account.getAvailableBalance());
        response.setHoldBalance(account.getHoldBalance());
        response.setOverdraftLimit(account.getOverdraftLimit());
        response.setOverdraftUsed(account.getOverdraftUsed());
        response.setMinimumBalance(account.getMinimumBalance());
        response.setInterestRate(account.getInterestRate());
        response.setBranchId(account.getBranchId());
        response.setBranchName(account.getBranchName());
        response.setAccountHolderName(account.getAccountHolderName());
        response.setAccountHolderEmail(account.getAccountHolderEmail());
        response.setAccountHolderPhone(account.getAccountHolderPhone());
        response.setEnableNotifications(account.isEnableNotifications());
        response.setEnableSMSAlerts(account.isEnableSMSAlerts());
        response.setEnableEmailAlerts(account.isEnableEmailAlerts());
        response.setLastTransactionDate(account.getLastTransactionDate());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());
        response.setInterestBearing(account.isInterestBearing());
        response.setOverdraftEnabled(account.isOverdraftAvailable());
        
        return response;
    }
}
