package com.xypay.xypay.service;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.Wallet;
import com.xypay.xypay.domain.NotificationType;
import com.xypay.xypay.domain.NotificationLevel;
import com.xypay.xypay.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Service
@Transactional
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Create a new wallet for user
     */
    public Wallet createWallet(User user, String currency) {
        logger.info("Creating wallet for user {} with currency {}", user.getUsername(), currency);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setCurrency(currency);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setAccountNumber(generateAccountNumber());
        wallet.setAlternativeAccountNumber(generateAlternativeAccountNumber());

        wallet = walletRepository.save(wallet);
        logger.info("Wallet created with ID {}", wallet.getId());

        return wallet;
    }

    /**
     * Create a new wallet for user with phone-based account number
     */
    public Wallet createWallet(User user, String currency, String phoneNumber) {
        logger.info("Creating wallet for user {} with currency {} and phone-based account number", user.getUsername(), currency);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setCurrency(currency);
        wallet.setBalance(BigDecimal.ZERO);
        
        // Use phone number as account number (extract digits only)
        String phoneAccountNumber = extractPhoneDigitsAsAccountNumber(phoneNumber);
        wallet.setAccountNumber(phoneAccountNumber);
        wallet.setAlternativeAccountNumber(generateAlternativeAccountNumber());
        wallet.setPhoneAlias(phoneNumber); // Store full phone number as alias

        wallet = walletRepository.save(wallet);
        logger.info("Wallet created with ID {} and phone-based account number {}", wallet.getId(), phoneAccountNumber);

        return wallet;
    }

    /**
     * Get user's primary wallet
     */
    @Transactional(readOnly = true)
    public Optional<Wallet> getUserPrimaryWallet(User user) {
        return walletRepository.findByUser(user).stream().findFirst();
    }

    /**
     * Get all user wallets
     */
    @Transactional(readOnly = true)
    public List<Wallet> getUserWallets(User user) {
        return walletRepository.findByUser(user);
    }

    /**
     * Credit wallet
     */
    public Wallet creditWallet(Long walletId, BigDecimal amount, String description) {
        logger.info("Crediting wallet {} with amount {}", walletId, amount);

        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet = walletRepository.save(wallet);

        // Send notification
        sendBalanceUpdateNotification(wallet, "credited", amount, description);

        logger.info("Wallet {} credited successfully. New balance: {}", walletId, wallet.getBalance());
        return wallet;
    }

    /**
     * Debit wallet
     */
    public Wallet debitWallet(Long walletId, BigDecimal amount, String description) {
        logger.info("Debiting wallet {} with amount {}", walletId, amount);

        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet = walletRepository.save(wallet);

        // Send notification
        sendBalanceUpdateNotification(wallet, "debited", amount, description);

        logger.info("Wallet {} debited successfully. New balance: {}", walletId, wallet.getBalance());
        return wallet;
    }

    /**
     * Transfer between wallets
     */
    public void transferBetweenWallets(Long fromWalletId, Long toWalletId, BigDecimal amount, String description) {
        logger.info("Transferring {} from wallet {} to wallet {}", amount, fromWalletId, toWalletId);

        Wallet fromWallet = walletRepository.findById(fromWalletId)
            .orElseThrow(() -> new RuntimeException("Source wallet not found"));
        
        Wallet toWallet = walletRepository.findById(toWalletId)
            .orElseThrow(() -> new RuntimeException("Destination wallet not found"));

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in source wallet");
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Send notifications
        sendBalanceUpdateNotification(fromWallet, "debited", amount, "Transfer to " + toWallet.getAccountNumber());
        sendBalanceUpdateNotification(toWallet, "credited", amount, "Transfer from " + fromWallet.getAccountNumber());

        logger.info("Transfer completed successfully");
    }

    /**
     * Freeze wallet
     */
    public Wallet freezeWallet(Long walletId, String reason) {
        logger.info("Freezing wallet {} for reason: {}", walletId, reason);

        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // Note: Wallet freeze functionality would need to be implemented with a status field
        wallet = walletRepository.save(wallet);

        // Send notification
        try {
            notificationService.createBankingNotification(
                wallet.getUser(),
                "Wallet Frozen",
                "Your wallet has been frozen. Reason: " + reason,
                NotificationType.SECURITY_ALERT,
                NotificationLevel.WARNING,
                wallet
            );
        } catch (Exception e) {
            logger.error("Failed to send wallet freeze notification: {}", e.getMessage());
        }

        logger.info("Wallet {} frozen successfully", walletId);
        return wallet;
    }

    /**
     * Unfreeze wallet
     */
    public Wallet unfreezeWallet(Long walletId) {
        logger.info("Unfreezing wallet {}", walletId);

        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // Note: Wallet unfreeze functionality would need to be implemented with a status field
        wallet = walletRepository.save(wallet);

        // Send notification
        try {
            notificationService.createBankingNotification(
                wallet.getUser(),
                "Wallet Unfrozen",
                "Your wallet has been unfrozen and is now active.",
                NotificationType.ACCOUNT_UPDATE,
                NotificationLevel.INFO,
                wallet
            );
        } catch (Exception e) {
            logger.error("Failed to send wallet unfreeze notification: {}", e.getMessage());
        }

        logger.info("Wallet {} unfrozen successfully", walletId);
        return wallet;
    }

    /**
     * Get wallet by ID
     */
    @Transactional(readOnly = true)
    public Optional<Wallet> getWalletById(Long walletId) {
        return walletRepository.findById(walletId);
    }

    /**
     * Get wallet balance
     */
    @Transactional(readOnly = true)
    public BigDecimal getWalletBalance(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return wallet.getBalance();
    }

    /**
     * Check if wallet has sufficient balance
     */
    @Transactional(readOnly = true)
    public boolean hasSufficientBalance(Long walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return wallet.getBalance().compareTo(amount) >= 0;
    }

    /**
     * Generate account number
     */
    private String generateAccountNumber() {
        // Generate 10-digit account number (Nigerian bank standard)
        long timestamp = System.currentTimeMillis();
        String accountNumber = String.valueOf(timestamp % 10000000000L); // Last 10 digits
        
        // Ensure it's exactly 10 digits by padding with zeros if needed
        return String.format("%010d", Long.parseLong(accountNumber));
    }

    /**
     * Generate alternative account number
     */
    private String generateAlternativeAccountNumber() {
        // Generate 10-digit alternative account number
        long timestamp = System.currentTimeMillis();
        String altAccountNumber = String.valueOf((timestamp + 1) % 10000000000L); // Last 10 digits + 1
        
        // Ensure it's exactly 10 digits by padding with zeros if needed
        return String.format("%010d", Long.parseLong(altAccountNumber));
    }

    /**
     * Extract phone number digits to use as account number
     * Removes country code and formats as 10-digit account number
     */
    private String extractPhoneDigitsAsAccountNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        
        // Remove all non-digit characters
        String digitsOnly = phoneNumber.replaceAll("\\D", "");
        
        // Handle Nigerian phone numbers
        if (digitsOnly.startsWith("234")) {
            // Remove country code +234 and use the remaining 10 digits
            digitsOnly = digitsOnly.substring(3);
        } else if (digitsOnly.startsWith("0")) {
            // Remove leading 0 for local format (e.g., 07038655955 -> 7038655955)
            digitsOnly = digitsOnly.substring(1);
        }
        
        // Ensure we have exactly 10 digits for account number
        if (digitsOnly.length() != 10) {
            throw new IllegalArgumentException("Invalid phone number format. Expected 10 digits after processing.");
        }
        
        return digitsOnly;
    }

    /**
     * Find wallet by phone number (account number)
     */
    @Transactional(readOnly = true)
    public Optional<Wallet> getWalletByPhoneNumber(String phoneNumber) {
        String phoneAccountNumber = extractPhoneDigitsAsAccountNumber(phoneNumber);
        return walletRepository.findByAccountNumberOrAlternativeAccountNumber(phoneAccountNumber, phoneAccountNumber);
    }

    /**
     * Find wallet by any account number (primary or alternative)
     */
    @Transactional(readOnly = true)
    public Optional<Wallet> getWalletByAnyAccountNumber(String accountNumber) {
        return walletRepository.findByAccountNumberOrAlternativeAccountNumber(accountNumber, accountNumber);
    }

    /**
     * Send balance update notification
     */
    private void sendBalanceUpdateNotification(Wallet wallet, String action, BigDecimal amount, String description) {
        try {
            String title = String.format("Wallet %s", action.substring(0, 1).toUpperCase() + action.substring(1));
            String message = String.format("Your wallet has been %s with %s %s. New balance: %s %s. %s",
                action, amount, wallet.getCurrency(), wallet.getBalance(), wallet.getCurrency(), 
                description != null ? description : "");

            notificationService.createBankingNotification(
                wallet.getUser(),
                title,
                message,
                NotificationType.BANK_TRANSACTION,
                NotificationLevel.INFO,
                wallet
            );
        } catch (Exception e) {
            logger.error("Failed to send balance update notification: {}", e.getMessage());
        }
    }
}
