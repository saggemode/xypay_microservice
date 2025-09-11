package com.xypay.account.service;

import com.xypay.account.domain.Account;
import com.xypay.account.domain.Wallet;
import com.xypay.account.repository.AccountRepository;
import com.xypay.account.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class AccountService {
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    // Wallet Management
    public Wallet createWallet(Long userId, String currency) {
        String accountNumber = generateAccountNumber();
        
        Wallet wallet = new Wallet(userId, accountNumber);
        wallet.setCurrency(currency);
        
        return walletRepository.save(wallet);
    }
    
    public Optional<Wallet> getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId);
    }
    
    public Optional<Wallet> getWalletByAccountNumber(String accountNumber) {
        return walletRepository.findByAccountNumber(accountNumber);
    }
    
    public Optional<Wallet> getWalletByPhoneAlias(String phoneAlias) {
        return walletRepository.findByPhoneAlias(phoneAlias);
    }
    
    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }
    
    public List<Wallet> getActiveWallets() {
        return walletRepository.findActiveWallets();
    }
    
    public Wallet updateWalletBalance(Long walletId, BigDecimal newBalance) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        wallet.setBalance(newBalance);
        return walletRepository.save(wallet);
    }
    
    public Wallet debitWallet(Long walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        wallet.debit(amount);
        return walletRepository.save(wallet);
    }
    
    public Wallet creditWallet(Long walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        wallet.credit(amount);
        return walletRepository.save(wallet);
    }
    
    public Wallet freezeWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        wallet.freeze();
        return walletRepository.save(wallet);
    }
    
    public Wallet unfreezeWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        wallet.unfreeze();
        return walletRepository.save(wallet);
    }
    
    public Wallet setPhoneAlias(Long walletId, String phoneAlias) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        wallet.setPhoneAlias(phoneAlias);
        return walletRepository.save(wallet);
    }
    
    // Account Management
    public Account createAccount(Long customerId, String accountType, String currency) {
        String accountNumber = generateAccountNumber();
        
        Account account = new Account(customerId, accountNumber, accountType);
        account.setCurrency(currency);
        
        return accountRepository.save(account);
    }
    
    public Optional<Account> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
    
    public List<Account> getAccountsByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }
    
    public List<Account> getActiveAccountsByCustomerId(Long customerId) {
        return accountRepository.findActiveAccountsByCustomerId(customerId);
    }
    
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
    
    public List<Account> getAccountsByStatus(String status) {
        return accountRepository.findByStatus(status);
    }
    
    public Account updateAccountBalance(Long accountId, BigDecimal ledgerBalance, BigDecimal availableBalance) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.setLedgerBalance(ledgerBalance);
        account.setAvailableBalance(availableBalance);
        return accountRepository.save(account);
    }
    
    public Account debitAccount(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.debit(amount);
        return accountRepository.save(account);
    }
    
    public Account creditAccount(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.credit(amount);
        return accountRepository.save(account);
    }
    
    public Account holdAmount(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.holdAmount(amount);
        return accountRepository.save(account);
    }
    
    public Account releaseHold(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.releaseHold(amount);
        return accountRepository.save(account);
    }
    
    public Account freezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.freeze();
        return accountRepository.save(account);
    }
    
    public Account unfreezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.unfreeze();
        return accountRepository.save(account);
    }
    
    public Account closeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        account.close();
        return accountRepository.save(account);
    }
    
    // Balance Inquiry
    public BigDecimal getWalletBalance(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        return wallet.getBalance();
    }
    
    public BigDecimal getAccountBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        return account.getAvailableBalance();
    }
    
    public boolean canDebitWallet(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId).orElse(null);
        return wallet != null && wallet.canDebit(amount);
    }
    
    public boolean canDebitAccount(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber).orElse(null);
        return account != null && account.canDebit(amount);
    }
    
    // Utility Methods
    private String generateAccountNumber() {
        // Generate 10-digit account number
        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder();
        
        // Start with bank code (e.g., "10" for XY Pay)
        accountNumber.append("10");
        
        // Add 8 random digits
        for (int i = 0; i < 8; i++) {
            accountNumber.append(random.nextInt(10));
        }
        
        return accountNumber.toString();
    }
}
