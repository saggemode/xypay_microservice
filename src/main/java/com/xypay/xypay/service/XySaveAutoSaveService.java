package com.xypay.xypay.service;

import com.xypay.xypay.domain.XySaveAccount;
import com.xypay.xypay.domain.XySaveTransaction;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.repository.XySaveAccountRepository;
import com.xypay.xypay.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class XySaveAutoSaveService {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveAutoSaveService.class);
    
    @Autowired
    private XySaveAccountRepository xySaveAccountRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private XySaveAccountService xySaveAccountService;
    
    @Autowired
    private XySaveTransactionService xySaveTransactionService;
    
    /**
     * Enable auto-save for user and sweep current wallet balance into XySave
     */
    @Transactional
    public XySaveAccount enableAutoSave(User user, BigDecimal percentage, BigDecimal minAmount) {
        try {
            XySaveAccount xySaveAccount = xySaveAccountService.getXySaveAccount(user);
            
            xySaveAccount.setAutoSaveEnabled(true);
            xySaveAccount.setAutoSavePercentage(percentage);
            xySaveAccount.setAutoSaveMinAmount(minAmount);
            xySaveAccountRepository.save(xySaveAccount);
            
            // Immediately sweep current wallet balance into XySave
            Wallet wallet = walletRepository.findByUser(user).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Wallet not found for user"));
            
            if (wallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                xySaveTransactionService.depositToXySave(
                    user,
                    wallet.getBalance(),
                    "Auto-save activation sweep"
                );
            }
            
            logger.info("Enabled auto-save for user {} at {}% and swept wallet balance", 
                user.getUsername(), percentage);
            return xySaveAccount;
            
        } catch (Exception e) {
            logger.error("Error enabling auto-save for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Disable auto-save for user
     */
    @Transactional
    public XySaveAccount disableAutoSave(User user) {
        try {
            XySaveAccount xySaveAccount = xySaveAccountService.getXySaveAccount(user);
            
            xySaveAccount.setAutoSaveEnabled(false);
            xySaveAccountRepository.save(xySaveAccount);
            
            logger.info("Disabled auto-save for user {}", user.getUsername());
            return xySaveAccount;
            
        } catch (Exception e) {
            logger.error("Error disabling auto-save for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Process auto-save when wallet receives money
     */
    @Transactional
    public XySaveTransaction processAutoSave(User user, BigDecimal walletTransactionAmount) {
        try {
            XySaveAccount xySaveAccount = xySaveAccountService.getXySaveAccount(user);
            
            if (!Boolean.TRUE.equals(xySaveAccount.getAutoSaveEnabled())) {
                return null;
            }
            
            // Calculate auto-save amount
            BigDecimal autoSaveAmount = walletTransactionAmount
                .multiply(xySaveAccount.getAutoSavePercentage())
                .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            
            // Check minimum amount
            if (autoSaveAmount.compareTo(xySaveAccount.getAutoSaveMinAmount()) < 0) {
                return null;
            }
            
            // Process auto-save
            return xySaveTransactionService.depositToXySave(
                user, 
                autoSaveAmount, 
                String.format("Auto-save (%s%% of ₦%s)", 
                    xySaveAccount.getAutoSavePercentage(), walletTransactionAmount)
            );
            
        } catch (Exception e) {
            logger.error("Error processing auto-save for user {}: {}", user.getUsername(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Get auto-save status for user
     */
    public AutoSaveStatus getAutoSaveStatus(User user) {
        try {
            XySaveAccount xySaveAccount = xySaveAccountService.getXySaveAccount(user);
            
            AutoSaveStatus status = new AutoSaveStatus();
            status.setEnabled(Boolean.TRUE.equals(xySaveAccount.getAutoSaveEnabled()));
            status.setPercentage(xySaveAccount.getAutoSavePercentage());
            status.setMinAmount(xySaveAccount.getAutoSaveMinAmount());
            
            return status;
            
        } catch (Exception e) {
            logger.error("Error getting auto-save status for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Auto-save status DTO
     */
    public static class AutoSaveStatus {
        private boolean enabled;
        private BigDecimal percentage;
        private BigDecimal minAmount;
        
        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public BigDecimal getPercentage() {
            return percentage;
        }
        
        public void setPercentage(BigDecimal percentage) {
            this.percentage = percentage;
        }
        
        public BigDecimal getMinAmount() {
            return minAmount;
        }
        
        public void setMinAmount(BigDecimal minAmount) {
            this.minAmount = minAmount;
        }
    }
}
