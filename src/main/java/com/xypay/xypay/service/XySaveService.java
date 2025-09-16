package com.xypay.xypay.service;

import com.xypay.xypay.domain.XySaveAccount;
import com.xypay.xypay.domain.XySaveTransaction;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.XySaveAccountRepository;
import com.xypay.xypay.repository.XySaveTransactionRepository;
import com.xypay.xypay.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class XySaveService {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveService.class);
    
    @Autowired
    private XySaveAccountRepository xySaveAccountRepository;
    
    @Autowired
    private XySaveTransactionRepository xySaveTransactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create or get XySave account for a user
     */
    @Transactional
    public XySaveAccount createOrGetXySaveAccount(UUID userId) {
        Optional<XySaveAccount> existingAccount = xySaveAccountRepository.findByUserId(userId);
        
        if (existingAccount.isPresent()) {
            return existingAccount.get();
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        XySaveAccount account = new XySaveAccount();
        account.setUser(user);
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setTotalInterestEarned(BigDecimal.ZERO);
        account.setIsActive(true);
        account.setAutoSaveEnabled(false);
        account.setAutoSavePercentage(new BigDecimal("10.00"));
        account.setAutoSaveMinAmount(new BigDecimal("100.00"));
        account.setDailyInterestRate(new BigDecimal("0.0004")); // ~15% annual
        
        XySaveAccount savedAccount = xySaveAccountRepository.save(account);
        logger.info("Created XySave account {} for user {}", savedAccount.getAccountNumber(), userId);
        
        return savedAccount;
    }
    
    /**
     * Get XySave account by user ID
     */
    public Optional<XySaveAccount> getXySaveAccount(UUID userId) {
        return xySaveAccountRepository.findByUserId(userId);
    }
    
    /**
     * Get all XySave accounts with pagination
     */
    public Page<XySaveAccount> getAllXySaveAccounts(Pageable pageable) {
        return xySaveAccountRepository.findAll(pageable);
    }
    
    /**
     * Create investment transaction
     */
    @Transactional
    public XySaveTransaction createInvestment(UUID userId, BigDecimal amount, String description) {
        XySaveAccount account = createOrGetXySaveAccount(userId);
        
        XySaveTransaction transaction = new XySaveTransaction();
        transaction.setXySaveAccount(account);
        transaction.setTransactionType(XySaveTransaction.TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(account.getBalance());
        transaction.setBalanceAfter(account.getBalance().add(amount));
        transaction.setReference(generateTransactionReference());
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        
        // Update account balance
        account.setBalance(account.getBalance().add(amount));
        xySaveAccountRepository.save(account);
        
        XySaveTransaction savedTransaction = xySaveTransactionRepository.save(transaction);
        logger.info("Created investment transaction {} for user {}: {}", 
            savedTransaction.getReference(), userId, amount);
        
        return savedTransaction;
    }
    
    /**
     * Process withdrawal from XySave account
     */
    @Transactional
    public XySaveTransaction processWithdrawal(UUID userId, BigDecimal amount, String description) {
        XySaveAccount account = xySaveAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("XySave account not found for user: " + userId));
        
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in XySave account");
        }
        
        XySaveTransaction transaction = new XySaveTransaction();
        transaction.setXySaveAccount(account);
        transaction.setTransactionType(XySaveTransaction.TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(account.getBalance());
        transaction.setBalanceAfter(account.getBalance().subtract(amount));
        transaction.setReference(generateTransactionReference());
        transaction.setDescription(description);
        transaction.setCreatedAt(LocalDateTime.now());
        
        // Update account balance
        account.setBalance(account.getBalance().subtract(amount));
        xySaveAccountRepository.save(account);
        
        XySaveTransaction savedTransaction = xySaveTransactionRepository.save(transaction);
        logger.info("Processed withdrawal transaction {} for user {}: {}", 
            savedTransaction.getReference(), userId, amount);
        
        return savedTransaction;
    }
    
    /**
     * Calculate and credit interest
     */
    @Transactional
    public void calculateAndCreditInterest(UUID userId) {
        XySaveAccount account = xySaveAccountRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("XySave account not found for user: " + userId));
        
        if (account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return; // No interest to calculate
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastCalculation = account.getLastInterestCalculation();
        
        if (lastCalculation == null) {
            lastCalculation = account.getCreatedAt();
        }
        
        // Calculate days since last interest calculation
        long daysSinceLastCalculation = java.time.Duration.between(lastCalculation, now).toDays();
        
        if (daysSinceLastCalculation > 0) {
            BigDecimal dailyInterest = account.getBalance().multiply(account.getDailyInterestRate());
            BigDecimal totalInterest = dailyInterest.multiply(BigDecimal.valueOf(daysSinceLastCalculation));
            
            if (totalInterest.compareTo(BigDecimal.ZERO) > 0) {
                // Create interest credit transaction
                XySaveTransaction interestTransaction = new XySaveTransaction();
                interestTransaction.setXySaveAccount(account);
                interestTransaction.setTransactionType(XySaveTransaction.TransactionType.INTEREST_CREDIT);
                interestTransaction.setAmount(totalInterest);
                interestTransaction.setBalanceBefore(account.getBalance());
                interestTransaction.setBalanceAfter(account.getBalance().add(totalInterest));
                interestTransaction.setReference(generateTransactionReference());
                interestTransaction.setDescription("Interest credit for " + daysSinceLastCalculation + " days");
                interestTransaction.setCreatedAt(now);
                
                // Update account
                account.setBalance(account.getBalance().add(totalInterest));
                account.setTotalInterestEarned(account.getTotalInterestEarned().add(totalInterest));
                account.setLastInterestCalculation(now);
                
                xySaveAccountRepository.save(account);
                xySaveTransactionRepository.save(interestTransaction);
                
                logger.info("Credited interest {} to XySave account {} for user {}", 
                    totalInterest, account.getAccountNumber(), userId);
            }
        }
    }
    
    /**
     * Get XySave statistics
     */
    public Map<String, Object> getXySaveStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Active investments count
        long activeInvestments = xySaveAccountRepository.count();
        stats.put("activeInvestments", activeInvestments);
        
        // Total investment amount
        BigDecimal totalInvestment = xySaveAccountRepository.findAll().stream()
            .map(XySaveAccount::getBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalInvestment", totalInvestment);
        
        // Average return rate (mock data for now)
        stats.put("averageReturn", 8.5);
        
        // Total returns generated
        BigDecimal totalReturns = xySaveAccountRepository.findAll().stream()
            .map(XySaveAccount::getTotalInterestEarned)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalReturns", totalReturns);
        
        // New investments today
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        long newInvestmentsToday = xySaveTransactionRepository.findByCreatedAtBetween(startOfDay, endOfDay)
            .stream()
            .filter(t -> t.getTransactionType() == XySaveTransaction.TransactionType.DEPOSIT)
            .count();
        stats.put("newInvestmentsToday", newInvestmentsToday);
        
        // Active investors count
        long activeInvestors = xySaveAccountRepository.findAll().stream()
            .filter(account -> account.getIsActive() && account.getBalance().compareTo(BigDecimal.ZERO) > 0)
            .count();
        stats.put("activeInvestors", activeInvestors);
        
        logger.info("XySave statistics calculated: active={}, totalInvestment={}, totalReturns={}", 
            activeInvestments, totalInvestment, totalReturns);
        
        return stats;
    }
    
    /**
     * Get recent XySave transactions
     */
    public List<Map<String, Object>> getRecentTransactions(int limit) {
        List<XySaveTransaction> transactions = xySaveTransactionRepository.findAll()
            .stream()
            .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
            .limit(limit)
            .toList();
        
        return transactions.stream().map(transaction -> {
            Map<String, Object> transactionData = new HashMap<>();
            transactionData.put("id", "INV-" + String.format("%06d", transaction.getId()));
            transactionData.put("investor", transaction.getXysaveAccount().getUser().getFirstName() + " " + 
                transaction.getXysaveAccount().getUser().getLastName());
            transactionData.put("amount", "₦" + String.format("%,.0f", transaction.getAmount().doubleValue()));
            transactionData.put("type", transaction.getTransactionType().toString());
            transactionData.put("description", transaction.getDescription());
            transactionData.put("date", transaction.getCreatedAt().toString());
            transactionData.put("status", "Active");
            return transactionData;
        }).toList();
    }
    
    /**
     * Update XySave settings
     */
    @Transactional
    public XySaveAccount updateSettings(UUID userId, BigDecimal dailyInterestRate, 
                                      Boolean autoSaveEnabled, BigDecimal autoSavePercentage, 
                                      BigDecimal autoSaveMinAmount) {
        XySaveAccount account = createOrGetXySaveAccount(userId);
        
        if (dailyInterestRate != null) {
            account.setDailyInterestRate(dailyInterestRate);
        }
        if (autoSaveEnabled != null) {
            account.setAutoSaveEnabled(autoSaveEnabled);
        }
        if (autoSavePercentage != null) {
            account.setAutoSavePercentage(autoSavePercentage);
        }
        if (autoSaveMinAmount != null) {
            account.setAutoSaveMinAmount(autoSaveMinAmount);
        }
        
        return xySaveAccountRepository.save(account);
    }
    
    /**
     * Generate unique account number
     */
    private String generateAccountNumber() {
        return "XYS" + System.currentTimeMillis();
    }
    
    /**
     * Get all users for dropdown selection
     */
    public List<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        return users.stream().map(user -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("name", user.getFirstName() + " " + user.getLastName());
            userData.put("email", user.getEmail());
            userData.put("username", user.getUsername());
            return userData;
        }).toList();
    }
    
    /**
     * Generate unique account number
     */
   
     
    
    /**
     * Generate unique transaction reference
     */
    private String generateTransactionReference() {
        return "XYS-TXN-" + System.currentTimeMillis();
    }
}
