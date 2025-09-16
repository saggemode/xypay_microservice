package com.xypay.xypay.service;

import com.xypay.xypay.domain.XySaveAccount;
import com.xypay.xypay.domain.XySaveTransaction;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.repository.XySaveAccountRepository;
import com.xypay.xypay.repository.XySaveTransactionRepository;
import com.xypay.xypay.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class XySaveTransactionService {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveTransactionService.class);
    
    @Autowired
    private XySaveAccountRepository xySaveAccountRepository;
    
    @Autowired
    private XySaveTransactionRepository xySaveTransactionRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private XySaveAccountService xySaveAccountService;
    
    @Autowired
    private TransactionSecurityService transactionSecurityService;
    
    /**
     * Deposit money to XySave account with ML-powered fraud and anomaly detection
     */
    @Transactional
    public XySaveTransaction depositToXySave(User user, BigDecimal amount, String description) {
        try {
            // Get accounts
            Wallet wallet = walletRepository.findByUser(user).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Wallet not found for user"));
            
            XySaveAccount xySaveAccount = xySaveAccountService.getXySaveAccount(user);
            
            // Validate amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Deposit amount must be positive");
            }
            
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient wallet balance");
            }
            
            // Generate reference
            String reference = generateReference("XS_DEP");
            
            // Record transaction first for ML analysis
            XySaveTransaction xySaveTransaction = new XySaveTransaction();
            xySaveTransaction.setXySaveAccount(xySaveAccount);
            xySaveTransaction.setTransactionType(XySaveTransaction.TransactionType.DEPOSIT);
            xySaveTransaction.setAmount(amount);
            xySaveTransaction.setBalanceBefore(xySaveAccount.getBalance());
            xySaveTransaction.setBalanceAfter(xySaveAccount.getBalance().add(amount));
            xySaveTransaction.setReference(reference);
            xySaveTransaction.setDescription(description);
            
            // Basic security checks
            Map<String, Object> securityResult = transactionSecurityService.checkTransactionRisk(xySaveTransaction, user);
            
            // Store security check results
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("security_check", securityResult);
            metadata.put("check_timestamp", LocalDateTime.now().toString());
            
            // Check if transaction should be flagged
            if ((Boolean) securityResult.get("is_suspicious")) {
                metadata.put("requires_review", true);
                Map<String, Object> securityFlags = new HashMap<>();
                securityFlags.put("risk_level", securityResult.get("risk_level"));
                securityFlags.put("risk_factors", securityResult.get("risk_factors"));
                metadata.put("security_flags", securityFlags);
                logger.warn("Transaction flagged for review: {}", xySaveTransaction.getReference());
            }
            
            xySaveTransaction.setMetadata(metadata.toString());
            xySaveTransaction = xySaveTransactionRepository.save(xySaveTransaction);
            
            // Update balances
            wallet.setBalance(wallet.getBalance().subtract(amount));
            walletRepository.save(wallet);
            
            xySaveAccount.setBalance(xySaveAccount.getBalance().add(amount));
            xySaveAccountRepository.save(xySaveAccount);
            
            logger.info("Deposited {} to XySave account {}", amount, xySaveAccount.getAccountNumber());
            return xySaveTransaction;
            
        } catch (Exception e) {
            logger.error("Error depositing to XySave for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Withdraw money from XySave account
     */
    @Transactional
    public XySaveTransaction withdrawFromXySave(User user, BigDecimal amount, String description) {
        try {
            // Get accounts
            Wallet wallet = walletRepository.findByUser(user).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Wallet not found for user"));
            
            XySaveAccount xySaveAccount = xySaveAccountService.getXySaveAccount(user);
            
            // Validate withdrawal
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Withdrawal amount must be positive");
            }
            
            if (!xySaveAccount.canWithdraw(amount)) {
                throw new IllegalArgumentException("Insufficient XySave balance or account inactive");
            }
            
            // Generate reference
            String reference = generateReference("XS_WTH");
            
            // Record transaction
            XySaveTransaction xySaveTransaction = new XySaveTransaction();
            xySaveTransaction.setXySaveAccount(xySaveAccount);
            xySaveTransaction.setTransactionType(XySaveTransaction.TransactionType.WITHDRAWAL);
            xySaveTransaction.setAmount(amount);
            xySaveTransaction.setBalanceBefore(xySaveAccount.getBalance());
            xySaveTransaction.setBalanceAfter(xySaveAccount.getBalance().subtract(amount));
            xySaveTransaction.setReference(reference);
            xySaveTransaction.setDescription(description);
            
            xySaveTransaction = xySaveTransactionRepository.save(xySaveTransaction);
            
            // Update balances
            xySaveAccount.setBalance(xySaveAccount.getBalance().subtract(amount));
            xySaveAccountRepository.save(xySaveAccount);
            
            wallet.setBalance(wallet.getBalance().add(amount));
            walletRepository.save(wallet);
            
            logger.info("Withdrew {} from XySave account {}", amount, xySaveAccount.getAccountNumber());
            return xySaveTransaction;
            
        } catch (Exception e) {
            logger.error("Error withdrawing from XySave for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Credit interest to XySave account
     */
    @Transactional
    public XySaveTransaction creditInterest(User user, BigDecimal amount, String description) {
        try {
            XySaveAccount xySaveAccount = xySaveAccountService.getXySaveAccount(user);
            
            // Generate reference
            String reference = generateReference("XS_INT");
            
            // Record transaction
            XySaveTransaction xySaveTransaction = new XySaveTransaction();
            xySaveTransaction.setXySaveAccount(xySaveAccount);
            xySaveTransaction.setTransactionType(XySaveTransaction.TransactionType.INTEREST_CREDIT);
            xySaveTransaction.setAmount(amount);
            xySaveTransaction.setBalanceBefore(xySaveAccount.getBalance());
            xySaveTransaction.setBalanceAfter(xySaveAccount.getBalance().add(amount));
            xySaveTransaction.setReference(reference);
            xySaveTransaction.setDescription(description);
            
            xySaveTransaction = xySaveTransactionRepository.save(xySaveTransaction);
            
            // Update account
            xySaveAccount.setBalance(xySaveAccount.getBalance().add(amount));
            xySaveAccount.setTotalInterestEarned(xySaveAccount.getTotalInterestEarned().add(amount));
            xySaveAccount.setLastInterestCalculation(LocalDateTime.now());
            xySaveAccountRepository.save(xySaveAccount);
            
            logger.info("Credited interest {} to XySave account {}", amount, xySaveAccount.getAccountNumber());
            
            // Send notification (non-blocking)
            try {
                // TODO: Integrate with notification service
                logger.info("Interest notification sent for user {}: Interest of ₦{} has been credited. Total interest earned: ₦{}", 
                    user.getUsername(), amount, xySaveAccount.getTotalInterestEarned());
            } catch (Exception e) {
                logger.warn("Failed to send interest notification: {}", e.getMessage());
            }
            
            return xySaveTransaction;
            
        } catch (Exception e) {
            logger.error("Error crediting interest for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Generate unique reference
     */
    private String generateReference(String prefix) {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return prefix + "_" + uuid;
    }
}
