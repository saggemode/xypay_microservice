package com.xypay.account.controller;

import com.xypay.account.domain.Account;
import com.xypay.account.domain.Wallet;
import com.xypay.account.dto.AccountCreationRequest;
import com.xypay.account.dto.AccountResponse;
import com.xypay.account.enums.AccountStatus;
import com.xypay.account.enums.AccountType;
import com.xypay.account.enums.Currency;
import com.xypay.account.service.AccountFeesService;
import com.xypay.account.service.AccountService;
import com.xypay.account.service.CoreBankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private CoreBankingService coreBankingService;
    
    @Autowired
    private AccountFeesService accountFeesService;
    
    // Wallet Endpoints
    @PostMapping("/wallets")
    public ResponseEntity<Wallet> createWallet(@RequestBody Map<String, Object> walletData) {
        Long userId = Long.valueOf(walletData.get("userId").toString());
        String currency = (String) walletData.getOrDefault("currency", "NGN");
        
        Wallet wallet = accountService.createWallet(userId, currency);
        return ResponseEntity.ok(wallet);
    }
    
    @GetMapping("/wallets/user/{userId}")
    public ResponseEntity<Wallet> getWalletByUserId(@PathVariable Long userId) {
        Optional<Wallet> wallet = accountService.getWalletByUserId(userId);
        return wallet.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/wallets/account/{accountNumber}")
    public ResponseEntity<Wallet> getWalletByAccountNumber(@PathVariable String accountNumber) {
        Optional<Wallet> wallet = accountService.getWalletByAccountNumber(accountNumber);
        return wallet.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/wallets/phone/{phoneAlias}")
    public ResponseEntity<Wallet> getWalletByPhoneAlias(@PathVariable String phoneAlias) {
        Optional<Wallet> wallet = accountService.getWalletByPhoneAlias(phoneAlias);
        return wallet.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/wallets")
    public ResponseEntity<List<Wallet>> getAllWallets() {
        List<Wallet> wallets = accountService.getAllWallets();
        return ResponseEntity.ok(wallets);
    }
    
    @GetMapping("/wallets/active")
    public ResponseEntity<List<Wallet>> getActiveWallets() {
        List<Wallet> wallets = accountService.getActiveWallets();
        return ResponseEntity.ok(wallets);
    }
    
    @PutMapping("/wallets/{walletId}/balance")
    public ResponseEntity<Wallet> updateWalletBalance(@PathVariable Long walletId, @RequestBody Map<String, BigDecimal> balanceData) {
        Wallet wallet = accountService.updateWalletBalance(walletId, balanceData.get("balance"));
        return ResponseEntity.ok(wallet);
    }
    
    @PostMapping("/wallets/{walletId}/debit")
    public ResponseEntity<Wallet> debitWallet(@PathVariable Long walletId, @RequestBody Map<String, BigDecimal> amountData) {
        Wallet wallet = accountService.debitWallet(walletId, amountData.get("amount"));
        return ResponseEntity.ok(wallet);
    }
    
    @PostMapping("/wallets/{walletId}/credit")
    public ResponseEntity<Wallet> creditWallet(@PathVariable Long walletId, @RequestBody Map<String, BigDecimal> amountData) {
        Wallet wallet = accountService.creditWallet(walletId, amountData.get("amount"));
        return ResponseEntity.ok(wallet);
    }
    
    @PostMapping("/wallets/{walletId}/freeze")
    public ResponseEntity<Wallet> freezeWallet(@PathVariable Long walletId) {
        Wallet wallet = accountService.freezeWallet(walletId);
        return ResponseEntity.ok(wallet);
    }
    
    @PostMapping("/wallets/{walletId}/unfreeze")
    public ResponseEntity<Wallet> unfreezeWallet(@PathVariable Long walletId) {
        Wallet wallet = accountService.unfreezeWallet(walletId);
        return ResponseEntity.ok(wallet);
    }
    
    @PutMapping("/wallets/{walletId}/phone-alias")
    public ResponseEntity<Wallet> setPhoneAlias(@PathVariable Long walletId, @RequestBody Map<String, String> aliasData) {
        Wallet wallet = accountService.setPhoneAlias(walletId, aliasData.get("phoneAlias"));
        return ResponseEntity.ok(wallet);
    }
    
    // Account Endpoints
    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Map<String, Object> accountData) {
        Long customerId = Long.valueOf(accountData.get("customerId").toString());
        String accountType = (String) accountData.get("accountType");
        String currency = (String) accountData.getOrDefault("currency", "NGN");
        
        Account account = accountService.createAccount(customerId, accountType, currency);
        return ResponseEntity.ok(account);
    }
    
    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccountByAccountNumber(@PathVariable String accountNumber) {
        Optional<Account> account = accountService.getAccountByAccountNumber(accountNumber);
        return account.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Account>> getAccountsByCustomerId(@PathVariable Long customerId) {
        List<Account> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }
    
    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<List<Account>> getActiveAccountsByCustomerId(@PathVariable Long customerId) {
        List<Account> accounts = accountService.getActiveAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }
    
    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Account>> getAccountsByStatus(@PathVariable String status) {
        List<Account> accounts = accountService.getAccountsByStatus(status);
        return ResponseEntity.ok(accounts);
    }
    
    @PutMapping("/{accountId}/balance")
    public ResponseEntity<Account> updateAccountBalance(@PathVariable Long accountId, @RequestBody Map<String, BigDecimal> balanceData) {
        Account account = accountService.updateAccountBalance(
            accountId, 
            balanceData.get("ledgerBalance"), 
            balanceData.get("availableBalance")
        );
        return ResponseEntity.ok(account);
    }
    
    @PostMapping("/{accountId}/debit")
    public ResponseEntity<Account> debitAccount(@PathVariable Long accountId, @RequestBody Map<String, BigDecimal> amountData) {
        Account account = accountService.debitAccount(accountId, amountData.get("amount"));
        return ResponseEntity.ok(account);
    }
    
    @PostMapping("/{accountId}/credit")
    public ResponseEntity<Account> creditAccount(@PathVariable Long accountId, @RequestBody Map<String, BigDecimal> amountData) {
        Account account = accountService.creditAccount(accountId, amountData.get("amount"));
        return ResponseEntity.ok(account);
    }
    
    @PostMapping("/{accountId}/hold")
    public ResponseEntity<Account> holdAmount(@PathVariable Long accountId, @RequestBody Map<String, BigDecimal> amountData) {
        Account account = accountService.holdAmount(accountId, amountData.get("amount"));
        return ResponseEntity.ok(account);
    }
    
    @PostMapping("/{accountId}/release-hold")
    public ResponseEntity<Account> releaseHold(@PathVariable Long accountId, @RequestBody Map<String, BigDecimal> amountData) {
        Account account = accountService.releaseHold(accountId, amountData.get("amount"));
        return ResponseEntity.ok(account);
    }
    
    @PostMapping("/{accountId}/freeze")
    public ResponseEntity<Account> freezeAccount(@PathVariable Long accountId) {
        Account account = accountService.freezeAccount(accountId);
        return ResponseEntity.ok(account);
    }
    
    @PostMapping("/{accountId}/unfreeze")
    public ResponseEntity<Account> unfreezeAccount(@PathVariable Long accountId) {
        Account account = accountService.unfreezeAccount(accountId);
        return ResponseEntity.ok(account);
    }
    
    @PostMapping("/{accountId}/close")
    public ResponseEntity<Account> closeAccount(@PathVariable Long accountId) {
        Account account = accountService.closeAccount(accountId);
        return ResponseEntity.ok(account);
    }
    
    // Balance Inquiry Endpoints
    @GetMapping("/wallets/user/{userId}/balance")
    public ResponseEntity<Map<String, BigDecimal>> getWalletBalance(@PathVariable Long userId) {
        try {
            BigDecimal balance = accountService.getWalletBalance(userId);
            return ResponseEntity.ok(Map.of("balance", balance));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Note: Simple balance endpoint removed - use core banking balance endpoint below
    
    @PostMapping("/wallets/user/{userId}/can-debit")
    public ResponseEntity<Map<String, Boolean>> canDebitWallet(@PathVariable Long userId, @RequestBody Map<String, BigDecimal> amountData) {
        boolean canDebit = accountService.canDebitWallet(userId, amountData.get("amount"));
        return ResponseEntity.ok(Map.of("canDebit", canDebit));
    }
    
    @PostMapping("/{accountNumber}/can-debit")
    public ResponseEntity<Map<String, Boolean>> canDebitAccount(@PathVariable String accountNumber, @RequestBody Map<String, BigDecimal> amountData) {
        boolean canDebit = accountService.canDebitAccount(accountNumber, amountData.get("amount"));
        return ResponseEntity.ok(Map.of("canDebit", canDebit));
    }
    
    // ========== CORE BANKING ENDPOINTS ==========
    
    // Account Creation
    @PostMapping("/create")
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountCreationRequest request) {
        try {
            AccountResponse account = coreBankingService.createAccount(request);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Account Details
    @GetMapping("/{accountNumber}/details")
    public ResponseEntity<AccountResponse> getAccountDetails(@PathVariable String accountNumber) {
        try {
            AccountResponse account = coreBankingService.getAccountDetails(accountNumber);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Customer Accounts
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getCustomerAccounts(@PathVariable Long customerId) {
        try {
            List<AccountResponse> accounts = coreBankingService.getAccountsByCustomer(customerId);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Note: Transaction processing moved to Transaction Service
    
    // Balance Inquiry
    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<Map<String, Object>> getAccountBalance(@PathVariable String accountNumber) {
        try {
            BigDecimal availableBalance = coreBankingService.getAccountBalance(accountNumber);
            BigDecimal ledgerBalance = coreBankingService.getLedgerBalance(accountNumber);
            
            return ResponseEntity.ok(Map.of(
                "accountNumber", accountNumber,
                "availableBalance", availableBalance,
                "ledgerBalance", ledgerBalance,
                "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Note: Transaction history endpoints moved to Transaction Service
    
    // Account Status Management
    @PostMapping("/{accountNumber}/freeze")
    public ResponseEntity<AccountResponse> freezeAccount(@PathVariable String accountNumber) {
        try {
            AccountResponse account = coreBankingService.freezeAccount(accountNumber);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{accountNumber}/unfreeze")
    public ResponseEntity<AccountResponse> unfreezeAccount(@PathVariable String accountNumber) {
        try {
            AccountResponse account = coreBankingService.unfreezeAccount(accountNumber);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{accountNumber}/close")
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable String accountNumber) {
        try {
            AccountResponse account = coreBankingService.closeAccount(accountNumber);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Interest Calculation
    @PostMapping("/{accountNumber}/calculate-interest")
    public ResponseEntity<Map<String, String>> calculateInterest(@PathVariable String accountNumber) {
        try {
            coreBankingService.calculateAndCreditInterest(accountNumber);
            return ResponseEntity.ok(Map.of("message", "Interest calculated and credited successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Fee Management
    @PostMapping("/{accountNumber}/fees/atm")
    public ResponseEntity<Map<String, String>> chargeATMFee(@PathVariable String accountNumber) {
        try {
            accountFeesService.chargeATMFee(accountNumber);
            return ResponseEntity.ok(Map.of("message", "ATM fee charged successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{accountNumber}/fees/transfer")
    public ResponseEntity<Map<String, String>> chargeTransferFee(@PathVariable String accountNumber) {
        try {
            accountFeesService.chargeTransferFee(accountNumber);
            return ResponseEntity.ok(Map.of("message", "Transfer fee charged successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{accountNumber}/fees/statement")
    public ResponseEntity<Map<String, String>> chargeStatementFee(@PathVariable String accountNumber) {
        try {
            accountFeesService.chargeStatementFee(accountNumber);
            return ResponseEntity.ok(Map.of("message", "Statement fee charged successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{accountNumber}/fees/card")
    public ResponseEntity<Map<String, String>> chargeCardFee(@PathVariable String accountNumber) {
        try {
            accountFeesService.chargeCardFee(accountNumber);
            return ResponseEntity.ok(Map.of("message", "Card fee charged successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Fee Waiver
    @PostMapping("/{accountNumber}/fees/waive/maintenance")
    public ResponseEntity<Map<String, String>> waiveMaintenanceFee(@PathVariable String accountNumber, @RequestBody Map<String, String> request) {
        try {
            String reason = request.getOrDefault("reason", "Administrative waiver");
            accountFeesService.waiveMaintenanceFee(accountNumber, reason);
            return ResponseEntity.ok(Map.of("message", "Maintenance fee waived successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{accountNumber}/fees/waive/overdraft")
    public ResponseEntity<Map<String, String>> waiveOverdraftFee(@PathVariable String accountNumber, @RequestBody Map<String, String> request) {
        try {
            String reason = request.getOrDefault("reason", "Administrative waiver");
            accountFeesService.waiveOverdraftFee(accountNumber, reason);
            return ResponseEntity.ok(Map.of("message", "Overdraft fee waived successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Account Types and Currencies
    @GetMapping("/account-types")
    public ResponseEntity<AccountType[]> getAccountTypes() {
        return ResponseEntity.ok(AccountType.values());
    }
    
    @GetMapping("/currencies")
    public ResponseEntity<Currency[]> getCurrencies() {
        return ResponseEntity.ok(Currency.values());
    }
    
    @GetMapping("/account-statuses")
    public ResponseEntity<AccountStatus[]> getAccountStatuses() {
        return ResponseEntity.ok(AccountStatus.values());
    }
    
    // Account Validation
    @PostMapping("/{accountNumber}/validate")
    public ResponseEntity<Map<String, Object>> validateAccount(@PathVariable String accountNumber) {
        try {
            AccountResponse account = coreBankingService.getAccountDetails(accountNumber);
            boolean canTransact = account.getStatus().canTransact();
            boolean meetsMinimumBalance = account.getLedgerBalance().compareTo(account.getMinimumBalance()) >= 0;
            boolean isOverdraftAvailable = account.isOverdraftEnabled() && 
                account.getOverdraftLimit().compareTo(account.getOverdraftUsed()) > 0;
            
            return ResponseEntity.ok(Map.of(
                "accountNumber", accountNumber,
                "isValid", true,
                "canTransact", canTransact,
                "meetsMinimumBalance", meetsMinimumBalance,
                "isOverdraftAvailable", isOverdraftAvailable,
                "status", account.getStatus().getDisplayName()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "accountNumber", accountNumber,
                "isValid", false,
                "error", e.getMessage()
            ));
        }
    }
}
