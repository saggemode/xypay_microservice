package com.xypay.xypay.service;

import com.xypay.xypay.domain.Account;
import com.xypay.xypay.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    public List<Map<String, Object>> getAllAccountsForOpenBanking() {
        return accountRepository.findAll().stream().map(a -> {
            Map<String, Object> accountMap = new HashMap<>();
            accountMap.put("accountId", a.getId());
            accountMap.put("iban", a.getAccountNumber());
            accountMap.put("currency", a.getCurrency());
            accountMap.put("type", a.getAccountType());
            return accountMap;
        }).collect(Collectors.toList());
    }
    public Map<String, Object> getBalanceForOpenBanking(Long accountId) {
        Account a = accountRepository.findById(accountId).orElse(null);
        return Map.of("accountId", accountId, "balance", a != null ? a.getLedgerBalance() : BigDecimal.ZERO, "currency", a != null ? a.getCurrency() : "");
    }
    public Map<String, Object> initiateOpenBankingPayment(Map<String, Object> payment) {
        return Map.of("paymentId", UUID.randomUUID().toString(), "status", "Accepted", "details", payment);
    }
    public Account createAccount(Long customerId, String currency, String accountType) {
        Account account = new Account();
        account.setCustomerId(customerId);
        account.setCurrency(currency);
        account.setAccountType(accountType);
        account.setAccountNumber("ACCT-" + UUID.randomUUID().toString().substring(0,8));
        account.setStatus("ACTIVE");
        account.setCreatedAt(LocalDateTime.now());
        account.setLedgerBalance(BigDecimal.ZERO);
        return accountRepository.save(account);
    }
    public Account getAccount(Long accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }
    public Account openAccount(Long customerId, String currency, String accountType) {
        return createAccount(customerId, currency, accountType);
    }
    public String closeAccount(Long accountId) {
        Optional<Account> opt = accountRepository.findById(accountId);
        if (opt.isPresent()) {
            Account account = opt.get();
            account.setStatus("CLOSED");
            accountRepository.save(account);
            return "Account closed.";
        }
        return "Account not found.";
    }
    public String getAccountStatus(Long accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        return account != null ? account.getStatus() : "NOT_FOUND";
    }
}