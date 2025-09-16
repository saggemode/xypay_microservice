package com.xypay.xypay.service;

import com.xypay.xypay.domain.Transaction;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.event.TransactionEvent;
import com.xypay.xypay.exception.DuplicateTransactionException;
import com.xypay.xypay.exception.WalletNotFoundException;
import com.xypay.xypay.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransactionService {
    @Autowired
    private WalletService walletService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AMLService amlService;
    @Autowired
    private AuditTrailService auditTrailService;
    @Autowired(required = false)
    private KafkaEventService kafkaEventService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    // Using a cache with time-based expiration would be better in production
    private final Map<String, LocalDateTime> processedReferences = new HashMap<>();
    private static final long REFERENCE_EXPIRATION_HOURS = 24; // References expire after 24 hours

    @Transactional
    public Transaction processTransaction(UUID walletId, BigDecimal amount, String currency, String type, String direction, String reference) {
        // Clean up expired references
        cleanupExpiredReferences();
        
        if (processedReferences.containsKey(reference)) {
            throw new DuplicateTransactionException("Duplicate transaction: " + reference);
        }
        processedReferences.put(reference, LocalDateTime.now());
        
        Optional<Wallet> walletOpt = walletService.getWalletById(walletId);
        if (walletOpt.isEmpty()) throw new WalletNotFoundException("Wallet not found: " + walletId);
        Wallet wallet = walletOpt.get();
        if (direction.equals("CREDIT")) {
            wallet.setBalance(wallet.getBalance().add(amount));
        } else if (direction.equals("DEBIT")) {
            wallet.setBalance(wallet.getBalance().subtract(amount));
        }
        Transaction tx = new Transaction();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setCurrency(currency);
        tx.setType(type);
        tx.setDirection(direction);
        tx.setReference(reference);
        tx.setStatus("SUCCESS");
        transactionRepository.save(tx);
        
        // Publish TransactionEvent (equivalent to Django post_save signal)
        eventPublisher.publishEvent(new TransactionEvent(this, tx, true));
        
        // AML check
        amlService.checkTransaction(tx);
        // Audit log
        auditTrailService.logEvent("TRANSACTION", "Processed transaction: " + tx.getId() + ", amount: " + amount);
        // Kafka event
        if (kafkaEventService != null) {
            kafkaEventService.publishEvent("transactions", String.valueOf(tx.getId()), "Transaction processed: " + tx.getId());
        }
        return tx;
    }
    @Transactional
    public Transaction reverseTransaction(UUID transactionId) {
        Transaction tx = transactionRepository.findById(transactionId).orElse(null);
        if (tx != null) {
            // Get the wallet and update the balance in the opposite direction
            Wallet wallet = tx.getWallet();
            if (wallet != null) {
                if ("CREDIT".equals(tx.getDirection())) {
                    // If original was CREDIT, we need to DEBIT the wallet
                    wallet.setBalance(wallet.getBalance().subtract(tx.getAmount()));
                } else if ("DEBIT".equals(tx.getDirection())) {
                    // If original was DEBIT, we need to CREDIT the wallet
                    wallet.setBalance(wallet.getBalance().add(tx.getAmount()));
                }
            }
            
            tx.setReference("REVERSAL-" + tx.getReference());
            tx.setStatus("REVERSED");
            transactionRepository.save(tx);
            auditTrailService.logEvent("TRANSACTION_REVERSAL", "Reversed transaction: " + tx.getId());
            if (kafkaEventService != null) {
                kafkaEventService.publishEvent("transactions", String.valueOf(tx.getId()), "Transaction reversed: " + tx.getId());
            }
        }
        return tx;
    }
    public Transaction getTransaction(UUID transactionId) {
        return transactionRepository.findById(transactionId).orElse(null);
    }
    
    private void cleanupExpiredReferences() {
        LocalDateTime expirationThreshold = LocalDateTime.now().minusHours(REFERENCE_EXPIRATION_HOURS);
        processedReferences.entrySet().removeIf(entry -> entry.getValue().isBefore(expirationThreshold));
    }
    public List<Map<String, Object>> getTransactionsForOpenBanking(UUID walletId) {
        // Find wallet first, then get transactions
        Optional<Wallet> walletOpt = walletService.getWalletById(walletId);
        if (walletOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Use a custom approach to find transactions by wallet
        return transactionRepository.findAll().stream()
            .filter(t -> t.getWallet() != null && t.getWallet().getId().equals(walletId))
            .map(t -> {
                Map<String, Object> transactionMap = new HashMap<>();
                transactionMap.put("transactionId", String.valueOf(t.getId()));
                transactionMap.put("amount", t.getAmount());
                transactionMap.put("currency", t.getCurrency());
                transactionMap.put("description", t.getReference());
                return transactionMap;
            })
            .collect(Collectors.toList());
    }
    public Transaction deposit(UUID walletId, BigDecimal amount, String currency, String reference) {
        return processTransaction(walletId, amount, currency, "DEPOSIT", "CREDIT", reference);
    }
    public Transaction withdraw(UUID walletId, BigDecimal amount, String currency, String reference) {
        return processTransaction(walletId, amount, currency, "WITHDRAWAL", "DEBIT", reference);
    }
    @Transactional
    public Transaction transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount, String currency, String reference) {
        withdraw(fromWalletId, amount, currency, reference + "-DEBIT");
        return deposit(toWalletId, amount, currency, reference + "-CREDIT");
    }
}