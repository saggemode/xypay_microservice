package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.SmartEarnAccountRepository;
import com.xypay.xypay.repository.SmartEarnTransactionRepository;
import com.xypay.xypay.repository.SmartEarnInterestHistoryRepository;
import com.xypay.xypay.repository.WalletRepository;
import com.xypay.xypay.repository.TransactionRepository;
import com.xypay.xypay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;

@Service
@Transactional
public class SmartEarnService {
    
    private static final Logger logger = LoggerFactory.getLogger(SmartEarnService.class);
    
    @Autowired
    private SmartEarnAccountRepository smartEarnAccountRepository;
    
    @Autowired
    private SmartEarnTransactionRepository smartEarnTransactionRepository;
    
    @Autowired
    private SmartEarnInterestHistoryRepository smartEarnInterestHistoryRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create a new SmartEarn account for a user
     */
    public SmartEarnAccount createSmartEarnAccount(UUID userId) {
        // Check if user already has a SmartEarn account
        Optional<SmartEarnAccount> existingAccount = smartEarnAccountRepository.findByUserId(userId);
        if (existingAccount.isPresent()) {
            throw new RuntimeException("User already has a SmartEarn account");
        }
        
        // Get user's wallet
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        List<Wallet> wallets = walletRepository.findByUser(user);
        if (wallets.isEmpty()) {
            throw new RuntimeException("User wallet not found");
        }
        Wallet wallet = wallets.get(0);
        
        // Create SmartEarn account
        SmartEarnAccount account = new SmartEarnAccount();
        account.setUser(wallet.getUser());
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setTotalInterestEarned(BigDecimal.ZERO);
        account.setIsActive(true);
        
        return smartEarnAccountRepository.save(account);
    }
    
    /**
     * Deposit money into SmartEarn account
     */
    public SmartEarnTransaction deposit(UUID userId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }
        
        // Get SmartEarn account
        SmartEarnAccount account = smartEarnAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("SmartEarn account not found"));
        
        // Get user's wallet
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        List<Wallet> wallets = walletRepository.findByUser(user);
        if (wallets.isEmpty()) {
            throw new RuntimeException("User wallet not found");
        }
        Wallet wallet = wallets.get(0);
        
        // Check wallet balance
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance");
        }
        
        // Calculate processing fee
        BigDecimal processingFee = account.calculateProcessingFee(amount);
        BigDecimal netAmount = amount.subtract(processingFee);
        
        // Create transaction
        SmartEarnTransaction transaction = new SmartEarnTransaction();
        transaction.setSmartEarnAccount(account);
        transaction.setTransactionType(SmartEarnTransaction.TransactionType.DEPOSIT);
        transaction.setStatus(SmartEarnTransaction.TransactionStatus.PENDING);
        transaction.setAmount(amount);
        transaction.setProcessingFee(processingFee);
        transaction.setBalanceBefore(account.getBalance());
        transaction.setBalanceAfter(account.getBalance().add(netAmount));
        transaction.setReference(generateTransactionReference());
        transaction.setDescription(description);
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setConfirmationDate(account.getConfirmationDate(transaction.getTransactionTime()));
        
        // Save transaction
        transaction = smartEarnTransactionRepository.save(transaction);
        
        // Process the deposit
        processDeposit(account, wallet, transaction);
        
        return transaction;
    }
    
    /**
     * Withdraw money from SmartEarn account
     */
    public SmartEarnTransaction withdraw(UUID userId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }
        
        // Get SmartEarn account
        SmartEarnAccount account = smartEarnAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("SmartEarn account not found"));
        
        // Check account balance
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient SmartEarn balance");
        }
        
        // Get user's wallet
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        List<Wallet> wallets = walletRepository.findByUser(user);
        if (wallets.isEmpty()) {
            throw new RuntimeException("User wallet not found");
        }
        Wallet wallet = wallets.get(0);
        
        // Create transaction
        SmartEarnTransaction transaction = new SmartEarnTransaction();
        transaction.setSmartEarnAccount(account);
        transaction.setTransactionType(SmartEarnTransaction.TransactionType.WITHDRAWAL);
        transaction.setStatus(SmartEarnTransaction.TransactionStatus.PENDING);
        transaction.setAmount(amount);
        transaction.setProcessingFee(BigDecimal.ZERO); // No fee for withdrawals
        transaction.setBalanceBefore(account.getBalance());
        transaction.setBalanceAfter(account.getBalance().subtract(amount));
        transaction.setReference(generateTransactionReference());
        transaction.setDescription(description);
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setConfirmationDate(account.getConfirmationDate(transaction.getTransactionTime()));
        
        // Save transaction
        transaction = smartEarnTransactionRepository.save(transaction);
        
        // Process the withdrawal
        processWithdrawal(account, wallet, transaction);
        
        return transaction;
    }
    
    /**
     * Process deposit transaction
     */
    private void processDeposit(SmartEarnAccount account, Wallet wallet, SmartEarnTransaction transaction) {
        try {
            // Debit wallet
            wallet.setBalance(wallet.getBalance().subtract(transaction.getAmount()));
            walletRepository.save(wallet);
            
            // Create wallet transaction
            Transaction walletTransaction = new Transaction();
            walletTransaction.setWallet(wallet);
            walletTransaction.setReference(transaction.getReference());
            walletTransaction.setAmount(transaction.getAmount());
            walletTransaction.setType("DEBIT");
            walletTransaction.setChannel("SMARTEARN_DEPOSIT");
            walletTransaction.setDescription("SmartEarn Deposit: " + transaction.getDescription());
            walletTransaction.setStatus("SUCCESS");
            walletTransaction.setBalanceAfter(wallet.getBalance());
            walletTransaction.setCurrency("NGN");
            walletTransaction.setDirection("DEBIT");
            walletTransaction.setProcessedAt(LocalDateTime.now());
            
            transactionRepository.save(walletTransaction);
            
            // Credit SmartEarn account
            account.setBalance(account.getBalance().add(transaction.getNetAmount()));
            smartEarnAccountRepository.save(account);
            
            // Update transaction status
            transaction.setStatus(SmartEarnTransaction.TransactionStatus.SUCCESS);
            transaction.setProcessedAt(LocalDateTime.now());
            smartEarnTransactionRepository.save(transaction);
            
            logger.info("SmartEarn deposit processed successfully: {}", transaction.getReference());
            
        } catch (Exception e) {
            logger.error("Error processing SmartEarn deposit: {}", e.getMessage(), e);
            transaction.setStatus(SmartEarnTransaction.TransactionStatus.FAILED);
            smartEarnTransactionRepository.save(transaction);
            throw new RuntimeException("Failed to process deposit: " + e.getMessage());
        }
    }
    
    /**
     * Process withdrawal transaction
     */
    private void processWithdrawal(SmartEarnAccount account, Wallet wallet, SmartEarnTransaction transaction) {
        try {
            // Debit SmartEarn account
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            smartEarnAccountRepository.save(account);
            
            // Credit wallet
            wallet.setBalance(wallet.getBalance().add(transaction.getAmount()));
            walletRepository.save(wallet);
            
            // Create wallet transaction
            Transaction walletTransaction = new Transaction();
            walletTransaction.setWallet(wallet);
            walletTransaction.setReference(transaction.getReference());
            walletTransaction.setAmount(transaction.getAmount());
            walletTransaction.setType("CREDIT");
            walletTransaction.setChannel("SMARTEARN_WITHDRAWAL");
            walletTransaction.setDescription("SmartEarn Withdrawal: " + transaction.getDescription());
            walletTransaction.setStatus("SUCCESS");
            walletTransaction.setBalanceAfter(wallet.getBalance());
            walletTransaction.setCurrency("NGN");
            walletTransaction.setDirection("CREDIT");
            walletTransaction.setProcessedAt(LocalDateTime.now());
            
            // Set metadata with sender information
            try {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("transaction_id", transaction.getId().toString());
                metadata.put("transaction_type", "smartearn_withdrawal");
                metadata.put("sender_account", "SmartEarn Account");
                metadata.put("sender_name", "SmartEarn System");
                metadata.put("account_id", account.getId().toString());
                ObjectMapper mapper = new ObjectMapper();
                walletTransaction.setMetadata(mapper.writeValueAsString(metadata));
            } catch (Exception e) {
                logger.warn("Failed to serialize SmartEarn transaction metadata: {}", e.getMessage());
                walletTransaction.setMetadata("{}");
            }
            
            transactionRepository.save(walletTransaction);
            
            // Update transaction status
            transaction.setStatus(SmartEarnTransaction.TransactionStatus.SUCCESS);
            transaction.setProcessedAt(LocalDateTime.now());
            smartEarnTransactionRepository.save(transaction);
            
            logger.info("SmartEarn withdrawal processed successfully: {}", transaction.getReference());
            
        } catch (Exception e) {
            logger.error("Error processing SmartEarn withdrawal: {}", e.getMessage(), e);
            transaction.setStatus(SmartEarnTransaction.TransactionStatus.FAILED);
            smartEarnTransactionRepository.save(transaction);
            throw new RuntimeException("Failed to process withdrawal: " + e.getMessage());
        }
    }
    
    /**
     * Calculate and credit daily interest
     */
    public void calculateAndCreditDailyInterest(UUID userId) {
        SmartEarnAccount account = smartEarnAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("SmartEarn account not found"));
        
        if (account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return; // No interest to calculate
        }
        
        LocalDate today = LocalDate.now();
        
        // Check if interest already calculated for today
        if (smartEarnInterestHistoryRepository.existsBySmartEarnAccountAndInterestDate(account, today)) {
            return;
        }
        
        // Calculate interest
        BigDecimal dailyInterest = account.calculateDailyInterest();
        
        // Create interest history record
        SmartEarnInterestHistory interestHistory = new SmartEarnInterestHistory();
        interestHistory.setSmartEarnAccount(account);
        interestHistory.setInterestDate(today);
        interestHistory.setBalanceAtStart(account.getBalance());
        interestHistory.setBalanceAtEnd(account.getBalance());
        interestHistory.setAverageBalance(account.getBalance());
        interestHistory.setInterestRate(SmartEarnAccount.DAILY_INTEREST_RATE);
        interestHistory.setInterestEarned(dailyInterest);
        interestHistory.setIsCredited(false);
        
        smartEarnInterestHistoryRepository.save(interestHistory);
        
        // Update account's last interest calculation
        account.setLastInterestCalculation(LocalDateTime.now());
        smartEarnAccountRepository.save(account);
        
        logger.info("Daily interest calculated for SmartEarn account {}: {}", account.getAccountNumber(), dailyInterest);
    }
    
    /**
     * Credit accumulated interest
     */
    public void creditAccumulatedInterest(UUID userId) {
        SmartEarnAccount account = smartEarnAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("SmartEarn account not found"));
        
        // Get uncredited interest
        List<SmartEarnInterestHistory> uncreditedInterest = smartEarnInterestHistoryRepository
            .findUncreditedInterestHistory(account);
        
        if (uncreditedInterest.isEmpty()) {
            return;
        }
        
        // Calculate total interest to credit
        BigDecimal totalInterest = uncreditedInterest.stream()
            .map(SmartEarnInterestHistory::getInterestEarned)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalInterest.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        // Create interest credit transaction
        SmartEarnTransaction interestTransaction = new SmartEarnTransaction();
        interestTransaction.setSmartEarnAccount(account);
        interestTransaction.setTransactionType(SmartEarnTransaction.TransactionType.INTEREST_CREDIT);
        interestTransaction.setStatus(SmartEarnTransaction.TransactionStatus.SUCCESS);
        interestTransaction.setAmount(totalInterest);
        interestTransaction.setProcessingFee(BigDecimal.ZERO);
        interestTransaction.setBalanceBefore(account.getBalance());
        interestTransaction.setBalanceAfter(account.getBalance().add(totalInterest));
        interestTransaction.setReference(generateTransactionReference());
        interestTransaction.setDescription("Daily Interest Credit");
        interestTransaction.setTransactionTime(LocalDateTime.now());
        interestTransaction.setProcessedAt(LocalDateTime.now());
        
        smartEarnTransactionRepository.save(interestTransaction);
        
        // Update account balance
        account.setBalance(account.getBalance().add(totalInterest));
        account.setTotalInterestEarned(account.getTotalInterestEarned().add(totalInterest));
        smartEarnAccountRepository.save(account);
        
        // Mark interest as credited
        uncreditedInterest.forEach(SmartEarnInterestHistory::markAsCredited);
        smartEarnInterestHistoryRepository.saveAll(uncreditedInterest);
        
        logger.info("Interest credited to SmartEarn account {}: {}", account.getAccountNumber(), totalInterest);
    }
    
    /**
     * Get SmartEarn account details
     */
    public SmartEarnAccount getAccount(UUID userId) {
        return smartEarnAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("SmartEarn account not found"));
    }
    
    /**
     * Get transaction history
     */
    public List<SmartEarnTransaction> getTransactionHistory(UUID userId, int limit) {
        SmartEarnAccount account = getAccount(userId);
        return smartEarnTransactionRepository.findBySmartEarnAccountOrderByTransactionTimeDesc(account)
            .stream()
            .limit(limit)
            .toList();
    }
    
    /**
     * Get interest history
     */
    public List<SmartEarnInterestHistory> getInterestHistory(UUID userId, int limit) {
        SmartEarnAccount account = getAccount(userId);
        return smartEarnInterestHistoryRepository.findBySmartEarnAccountOrderByInterestDateDesc(account)
            .stream()
            .limit(limit)
            .toList();
    }
    
    /**
     * Generate unique account number
     */
    private String generateAccountNumber() {
        String prefix = "SE";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf((int) (Math.random() * 1000));
        return prefix + timestamp.substring(timestamp.length() - 8) + random;
    }
    
    /**
     * Generate unique transaction reference
     */
    private String generateTransactionReference() {
        return "SE" + System.currentTimeMillis() + (int) (Math.random() * 1000);
    }
}
