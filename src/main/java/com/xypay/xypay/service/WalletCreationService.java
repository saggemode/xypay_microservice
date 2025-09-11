package com.xypay.xypay.service;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.UserProfile;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.repository.UserProfileRepository;
import com.xypay.xypay.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Optional;

/**
 * Service for creating wallets automatically when KYC is approved.
 * Equivalent to Django's wallet creation logic in kyc_signals.py
 */
@Service
public class WalletCreationService {
    
    private static final Logger logger = LoggerFactory.getLogger(WalletCreationService.class);
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private AuditTrailService auditTrailService;
    
    /**
     * Create wallet for user when KYC is approved.
     * Matches Django logic exactly from create_wallet_on_kyc_approval function.
     */
    @Transactional
    public void createWalletOnKYCApproval(User user) {
        logger.info("KYC approved for user {}, checking for existing wallet", user.getUsername());
        
        // Check if wallet already exists
        if (!walletRepository.findByUser(user).isEmpty()) {
            logger.info("Wallet already exists for user {}", user.getUsername());
            return;
        }
        
        logger.info("No existing wallet found, creating new wallet for user {}", user.getUsername());
        
        try {
            String accountNumber = generateAccountNumber(user);
            String alternativeAccountNumber = generateAlternativeAccountNumber();
            
            // Create wallet with default balance (0 NGN)
            Wallet wallet = new Wallet();
            wallet.setUser(user);
            wallet.setAccountNumber(accountNumber);
            wallet.setAlternativeAccountNumber(alternativeAccountNumber);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setCurrency("NGN");
            
            wallet = walletRepository.save(wallet);
            
            logger.info("Wallet created successfully for user {} with account number {}", 
                user.getUsername(), accountNumber);
            
            auditTrailService.logEvent("WALLET_CREATED", 
                String.format("Wallet created for user: %s with account number: %s", 
                    user.getUsername(), accountNumber));
                    
        } catch (Exception e) {
            logger.error("Error creating wallet for user {}: {}", user.getUsername(), e.getMessage());
            auditTrailService.logEvent("WALLET_CREATION_ERROR", 
                String.format("Error creating wallet for user %s: %s", user.getUsername(), e.getMessage()));
            throw new RuntimeException("Failed to create wallet", e);
        }
    }
    
    /**
     * Generate account number from phone number.
     * Matches Django logic exactly.
     */
    private String generateAccountNumber(User user) {
        try {
            // Try to get phone number from user profile
            Optional<UserProfile> profileOpt = userProfileRepository.findByUser(user);
            
            if (profileOpt.isPresent()) {
                UserProfile profile = profileOpt.get();
                String phoneNumber = profile.getPhone();
                
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    // Remove country code and get last 10 digits
                    String phoneStr = phoneNumber.replaceAll("\\D", ""); // Remove non-digits
                    
                    if (phoneStr.startsWith("234")) {
                        phoneStr = phoneStr.substring(3); // Remove 234
                    }
                    
                    // Ensure it's 10 digits
                    if (phoneStr.length() >= 10) {
                        return phoneStr.substring(phoneStr.length() - 10); // Take last 10 digits
                    } else {
                        // Pad with zeros if less than 10 digits
                        return String.format("%010d", Long.parseLong(phoneStr));
                    }
                }
            }
            
            // Generate a random account number if no phone number
            logger.warn("No phone number found for user {}, generating random account number", user.getUsername());
            return generateRandomAccountNumber();
            
        } catch (Exception e) {
            logger.warn("Error processing phone number for user {}, generating random account number: {}", 
                user.getUsername(), e.getMessage());
            return generateRandomAccountNumber();
        }
    }
    
    /**
     * Generate random 10-digit account number.
     */
    private String generateRandomAccountNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        
        // First digit should not be 0
        sb.append(random.nextInt(9) + 1);
        
        // Remaining 9 digits
        for (int i = 0; i < 9; i++) {
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
    }
    
    /**
     * Generate alternative account number.
     * Matches Django's generate_alternative_account_number function.
     */
    private String generateAlternativeAccountNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        
        // Generate 12-digit alternative account number
        for (int i = 0; i < 12; i++) {
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
    }
}
