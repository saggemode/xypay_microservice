package com.xypay.xypay.service;

import com.xypay.xypay.domain.Bank;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.repository.BankRepository;
import com.xypay.xypay.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for validating bank transfers.
 */
@Service
public class TransferValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransferValidationService.class);
    
    @Autowired
    private BankAccountService bankAccountService;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private BankRepository bankRepository;
    
    /**
     * Validate a bank transfer request.
     * 
     * @param user User making the transfer
     * @param amount Transfer amount
     * @param recipientAccount Recipient account number
     * @param recipientBankCode Recipient bank code
     * @return Validation result
     */
    public Map<String, Object> validateTransferRequest(User user, BigDecimal amount, 
                                                     String recipientAccount, String recipientBankCode) {
        try {
            // Check if user has sufficient balance
            List<Wallet> wallets = walletRepository.findByUser(user);
            if (wallets.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("is_valid", false);
                result.put("error", "Wallet not found. Complete KYC verification first.");
                return result;
            }
            
            Wallet wallet = wallets.get(0);
            if (wallet.getBalance().compareTo(amount) < 0) {
                Map<String, Object> result = new HashMap<>();
                result.put("is_valid", false);
                result.put("error", "Insufficient balance");
                result.put("current_balance", wallet.getBalance().toString());
                result.put("required_amount", amount.toString());
                return result;
            }
            
            // Check if recipient account is valid
            List<Map<String, Object>> banks = bankAccountService.searchBanksByAccountNumber(recipientAccount);
            Optional<Map<String, Object>> recipientBankOpt = banks.stream()
                .filter(bank -> recipientBankCode.equals(bank.get("bank_code")))
                .findFirst();
            
            if (recipientBankOpt.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("is_valid", false);
                result.put("error", "Invalid recipient account or bank");
                result.put("available_banks", banks.stream()
                    .map(bank -> bank.get("bank_code"))
                    .toList());
                return result;
            }
            
            Map<String, Object> recipientBank = recipientBankOpt.get();
            Map<String, Object> result = new HashMap<>();
            result.put("is_valid", true);
            result.put("recipient_name", recipientBank.get("account_name"));
            result.put("bank_name", recipientBank.get("bank_name"));
            result.put("verification_method", recipientBank.get("verification_method"));
            return result;
            
        } catch (Exception e) {
            logger.error("Error validating transfer request: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("is_valid", false);
            result.put("error", "Validation failed. Please try again.");
            return result;
        }
    }
    
    /**
     * Validate transfer amount against business rules.
     * 
     * @param amount Transfer amount
     * @param user User making the transfer
     * @return Validation result
     */
    public Map<String, Object> validateTransferAmount(BigDecimal amount, User user) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Check minimum amount
            BigDecimal minimumAmount = new BigDecimal("100"); // ₦100 minimum
            if (amount.compareTo(minimumAmount) < 0) {
                result.put("is_valid", false);
                result.put("error", "Transfer amount must be at least ₦" + minimumAmount);
                return result;
            }
            
            // Check maximum amount
            BigDecimal maximumAmount = new BigDecimal("10000000"); // ₦10M maximum
            if (amount.compareTo(maximumAmount) > 0) {
                result.put("is_valid", false);
                result.put("error", "Transfer amount cannot exceed ₦" + maximumAmount);
                return result;
            }
            
            // Check daily limit
            BigDecimal dailyLimit = getDailyTransferLimit(user);
            BigDecimal todayTransfers = getTodayTransferAmount(user);
            
            if (todayTransfers.add(amount).compareTo(dailyLimit) > 0) {
                result.put("is_valid", false);
                result.put("error", "Transfer would exceed daily limit of ₦" + dailyLimit);
                result.put("remaining_limit", dailyLimit.subtract(todayTransfers).toString());
                return result;
            }
            
            result.put("is_valid", true);
            result.put("daily_limit", dailyLimit.toString());
            result.put("used_today", todayTransfers.toString());
            result.put("remaining", dailyLimit.subtract(todayTransfers).toString());
            
        } catch (Exception e) {
            logger.error("Error validating transfer amount: {}", e.getMessage());
            result.put("is_valid", false);
            result.put("error", "Amount validation failed");
        }
        
        return result;
    }
    
    /**
     * Validate recipient account details.
     * 
     * @param recipientAccount Recipient account number
     * @param recipientBankCode Recipient bank code
     * @return Validation result
     */
    public Map<String, Object> validateRecipientAccount(String recipientAccount, String recipientBankCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Validate account number format
            if (recipientAccount == null || !recipientAccount.matches("\\d{10,11}")) {
                result.put("is_valid", false);
                result.put("error", "Invalid account number format");
                return result;
            }
            
            // Validate bank code
            Optional<Bank> bankOpt = bankRepository.findByCode(recipientBankCode);
            if (bankOpt.isEmpty()) {
                result.put("is_valid", false);
                result.put("error", "Invalid bank code");
                return result;
            }
            
            Bank bank = bankOpt.get();
            if (!bank.getActive()) {
                result.put("is_valid", false);
                result.put("error", "Bank is not active");
                return result;
            }
            
            // Verify account with bank
            List<Map<String, Object>> banks = bankAccountService.searchBanksByAccountNumber(recipientAccount);
            Optional<Map<String, Object>> recipientBankOpt = banks.stream()
                .filter(bankInfo -> recipientBankCode.equals(bankInfo.get("bank_code")))
                .findFirst();
            
            if (recipientBankOpt.isEmpty()) {
                result.put("is_valid", false);
                result.put("error", "Account not found in specified bank");
                return result;
            }
            
            Map<String, Object> recipientBank = recipientBankOpt.get();
            result.put("is_valid", true);
            result.put("account_name", recipientBank.get("account_name"));
            result.put("bank_name", recipientBank.get("bank_name"));
            result.put("is_verified", recipientBank.get("is_verified"));
            
        } catch (Exception e) {
            logger.error("Error validating recipient account: {}", e.getMessage());
            result.put("is_valid", false);
            result.put("error", "Account validation failed");
        }
        
        return result;
    }
    
    /**
     * Validate transfer against user's transaction history and patterns.
     * 
     * @param user User making the transfer
     * @param amount Transfer amount
     * @param recipientAccount Recipient account number
     * @return Validation result
     */
    public Map<String, Object> validateTransferPattern(User user, BigDecimal amount, String recipientAccount) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Check for unusual transfer patterns
            // This would integrate with your fraud detection service
            // For now, we'll do basic checks
            
            // Check if this is a new recipient
            boolean isNewRecipient = isNewRecipient(user, recipientAccount);
            if (isNewRecipient) {
                result.put("is_new_recipient", true);
                result.put("warning", "This is a new recipient. Please verify the account details.");
            }
            
            // Check if amount is unusually high
            BigDecimal averageTransfer = getAverageTransferAmount(user);
            if (averageTransfer.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal threshold = averageTransfer.multiply(new BigDecimal("3"));
                if (amount.compareTo(threshold) > 0) {
                    result.put("is_unusual_amount", true);
                    result.put("warning", "This amount is significantly higher than your average transfer");
                    result.put("average_transfer", averageTransfer.toString());
                }
            }
            
            result.put("is_valid", true);
            
        } catch (Exception e) {
            logger.error("Error validating transfer pattern: {}", e.getMessage());
            result.put("is_valid", false);
            result.put("error", "Pattern validation failed");
        }
        
        return result;
    }
    
    /**
     * Get daily transfer limit for a user.
     * 
     * @param user User to get limit for
     * @return Daily transfer limit
     */
    private BigDecimal getDailyTransferLimit(User user) {
        // In production, this would be based on user tier, KYC level, etc.
        // For now, return a default limit
        return new BigDecimal("1000000"); // ₦1M default limit
    }
    
    /**
     * Get today's transfer amount for a user.
     * 
     * @param user User to get amount for
     * @return Today's transfer amount
     */
    private BigDecimal getTodayTransferAmount(User user) {
        // TODO: Implement actual calculation from transaction history
        // For now, return zero
        return BigDecimal.ZERO;
    }
    
    /**
     * Check if recipient is new for the user.
     * 
     * @param user User to check for
     * @param recipientAccount Recipient account number
     * @return True if recipient is new
     */
    private boolean isNewRecipient(User user, String recipientAccount) {
        // TODO: Implement actual check from transaction history
        // For now, return false
        return false;
    }
    
    /**
     * Get average transfer amount for a user.
     * 
     * @param user User to get average for
     * @return Average transfer amount
     */
    private BigDecimal getAverageTransferAmount(User user) {
        // TODO: Implement actual calculation from transaction history
        // For now, return zero
        return BigDecimal.ZERO;
    }
}
