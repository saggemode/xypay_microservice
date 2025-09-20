package com.xypay.account.service;

import com.xypay.account.domain.Account;
import com.xypay.account.repository.AccountRepository;
import com.xypay.account.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@Transactional
public class AccountService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public List<Map<String, Object>> getAllAccountsForOpenBanking() {
        return walletRepository.findAll().stream().map(w -> {
            Map<String, Object> accountMap = new HashMap<>();
            accountMap.put("accountId", w.getId());
            accountMap.put("iban", w.getAccountNumber());
            accountMap.put("currency", w.getCurrency());
            accountMap.put("type", "WALLET");
            return accountMap;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getBalanceForOpenBanking(UUID accountId) {
        var wallet = walletRepository.findById(accountId).orElse(null);
        return Map.of(
            "accountId", accountId, 
            "balance", wallet != null ? wallet.getBalance() : BigDecimal.ZERO, 
            "currency", wallet != null ? wallet.getCurrency() : ""
        );
    }

    public Map<String, Object> initiateOpenBankingPayment(Map<String, Object> payment) {
        return Map.of(
            "paymentId", UUID.randomUUID().toString(), 
            "status", "Accepted", 
            "details", payment
        );
    }

    public Account createAccount(UUID customerId, String currency, String accountType) {
        Account account = new Account();
        account.setCustomerId(customerId);
        account.setCurrency(currency);
        account.setAccountType(accountType);
        account.setAccountNumber("ACCT-" + UUID.randomUUID().toString().substring(0,8));
        account.setStatus("ACTIVE");
        account.setCreatedAt(LocalDateTime.now());
        account.setLedgerBalance(BigDecimal.ZERO);
        
        Account savedAccount = accountRepository.save(account);
        
        // Publish account created event
        kafkaTemplate.send("account-events", "account-created", 
            String.format("Account %s created for customer %s", savedAccount.getId(), customerId));
        
        return savedAccount;
    }

    @Cacheable(value = "accounts", key = "#accountId")
    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    public Account openAccount(UUID customerId, String currency, String accountType) {
        return createAccount(customerId, currency, accountType);
    }

    public String closeAccount(UUID accountId) {
        Optional<Account> opt = accountRepository.findById(accountId);
        if (opt.isPresent()) {
            Account account = opt.get();
            account.setStatus("CLOSED");
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);
            
            // Publish account closed event
            kafkaTemplate.send("account-events", "account-closed", 
                String.format("Account %s closed", accountId));
            
            return "Account closed successfully.";
        }
        return "Account not found.";
    }

    public String getAccountStatus(UUID accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        return account != null ? account.getStatus() : "NOT_FOUND";
    }

    public List<Account> getAccountsByCustomer(UUID customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    public List<Account> getAllActiveAccounts() {
        return accountRepository.findByStatus("ACTIVE");
    }

    public BigDecimal getAccountBalance(UUID accountId) {
        Account account = getAccount(accountId);
        return account != null ? account.getLedgerBalance() : BigDecimal.ZERO;
    }

    public boolean updateAccountBalance(UUID accountId, BigDecimal newBalance) {
        Optional<Account> opt = accountRepository.findById(accountId);
        if (opt.isPresent()) {
            Account account = opt.get();
            account.setLedgerBalance(newBalance);
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);
            
            // Publish balance updated event
            kafkaTemplate.send("account-events", "balance-updated", 
                String.format("Account %s balance updated to %s", accountId, newBalance));
            
            return true;
        }
        return false;
    }
}
