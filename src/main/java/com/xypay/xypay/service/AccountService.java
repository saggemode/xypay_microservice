package com.xypay.xypay.service;

import com.xypay.xypay.domain.Account;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.repository.AccountRepository;
import com.xypay.xypay.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class AccountService {
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private WalletRepository walletRepository;

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
		Wallet w = walletRepository.findById(accountId).orElse(null);
		return Map.of("accountId", accountId, "balance", w != null ? w.getBalance() : BigDecimal.ZERO, "currency", w != null ? w.getCurrency() : "");
	}
	public Map<String, Object> initiateOpenBankingPayment(Map<String, Object> payment) {
		return Map.of("paymentId", UUID.randomUUID().toString(), "status", "Accepted", "details", payment);
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
		return accountRepository.save(account);
	}
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
			accountRepository.save(account);
			return "Account closed.";
		}
		return "Account not found.";
	}
	public String getAccountStatus(UUID accountId) {
		Account account = accountRepository.findById(accountId).orElse(null);
		return account != null ? account.getStatus() : "NOT_FOUND";
	}
}