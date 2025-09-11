package com.xypay.xypay.service;

import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.repository.WalletRepository;
import com.xypay.xypay.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class TellerService {
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TransactionRepository transactionRepository;

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
            return transactionRepository.findByWalletId(walletOpt.get().getId(), 
                org.springframework.data.domain.Pageable.unpaged()).getContent();
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
