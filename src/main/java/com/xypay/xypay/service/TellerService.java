package com.xypay.xypay.service;

import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.repository.WalletRepository;
import com.xypay.xypay.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TellerService {
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private ObjectMapper objectMapper;

    public String deposit(String accountNumber, BigDecimal amount) {
        Optional<Wallet> walletOpt = walletRepository.findByAccountNumberOrAlternativeAccountNumber(accountNumber, accountNumber);
        if (walletOpt.isEmpty()) {
            return "Account not found";
        }
        Wallet wallet = walletOpt.get();
        BigDecimal balance = wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;
        wallet.setBalance(balance.add(amount));
        walletRepository.save(wallet);
        Transaction tx = new Transaction();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType("DEPOSIT");
        tx.setStatus("SUCCESS");
        tx.setDirection("CREDIT");
        tx.setDescription("Teller Deposit: " + amount);
        tx.setChannel("TELLER");
        
        // Set metadata with sender information
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("transaction_type", "teller_deposit");
            metadata.put("sender_account", "Teller Counter");
            metadata.put("sender_name", "Teller");
            metadata.put("teller_account", accountNumber);
            metadata.put("amount", amount.toString());
            tx.setMetadata(objectMapper.writeValueAsString(metadata));
        } catch (Exception e) {
            // Log warning but don't fail the transaction
            System.err.println("Failed to serialize teller deposit metadata: " + e.getMessage());
            tx.setMetadata("{}");
        }
        
        transactionRepository.save(tx);
        return "Deposit successful. New balance: " + wallet.getBalance();
    }

    public BigDecimal getBalance(String accountNumber) {
        Optional<Wallet> walletOpt = walletRepository.findByAccountNumberOrAlternativeAccountNumber(accountNumber, accountNumber);
        return walletOpt.map(wallet -> wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO)
                .orElse(BigDecimal.ZERO);
    }

    public java.util.List<Transaction> getTransactionHistory(String accountNumber) {
        Optional<Wallet> walletOpt = walletRepository.findByAccountNumberOrAlternativeAccountNumber(accountNumber, accountNumber);
        if (walletOpt.isPresent()) {
            // Use a custom approach to find transactions by wallet
            return transactionRepository.findAll().stream()
                .filter(t -> t.getWallet() != null && t.getWallet().getId().equals(walletOpt.get().getId()))
                .collect(java.util.stream.Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }

    public String withdrawal(String accountNumber, BigDecimal amount) {
        Optional<Wallet> walletOpt = walletRepository.findByAccountNumberOrAlternativeAccountNumber(accountNumber, accountNumber);
        if (walletOpt.isEmpty()) {
            return "Account not found";
        }
        Wallet wallet = walletOpt.get();
        BigDecimal balance = wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;
        if (balance.compareTo(amount) < 0) {
            return "Insufficient funds";
        }
        wallet.setBalance(balance.subtract(amount));
        walletRepository.save(wallet);
        Transaction tx = new Transaction();
        tx.setWallet(wallet);
        tx.setAmount(amount.negate());
        tx.setType("WITHDRAWAL");
        tx.setStatus("SUCCESS");
        tx.setDirection("DEBIT");
        transactionRepository.save(tx);
        return "Withdrawal successful. New balance: " + wallet.getBalance();
    }

    public String checkCashing(String checkNumber, String accountNumber, BigDecimal amount) {
        Optional<Wallet> walletOpt = walletRepository.findByAccountNumber(accountNumber);
        if (walletOpt.isEmpty()) {
            return "Account not found";
        }
        Wallet wallet = walletOpt.get();
        BigDecimal balance = wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;
        if (balance.compareTo(amount) < 0) {
            return "Insufficient funds";
        }
        wallet.setBalance(balance.subtract(amount));
        walletRepository.save(wallet);
        Transaction tx = new Transaction();
        tx.setWallet(wallet);
        tx.setAmount(amount.negate());
        tx.setType("CHECK_CASHING");
        tx.setStatus("SUCCESS");
        tx.setDirection("DEBIT");
        tx.setReference(checkNumber); // Save check number as reference
        transactionRepository.save(tx);
        return "Check cashed successfully. New balance: " + wallet.getBalance();
    }

    public Optional<Wallet> accountLookup(String accountNumber) {
        return walletRepository.findByAccountNumber(accountNumber);
    }
}
