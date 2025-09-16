package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class SpendAndSaveService {
    
    @Autowired
    private SpendAndSaveAccountRepository spendAndSaveAccountRepository;
    
    @Autowired
    private SpendAndSaveTransactionRepository spendAndSaveTransactionRepository;
    
    @Autowired
    private SpendAndSaveSettingsRepository spendAndSaveSettingsRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private XySaveAccountRepository xySaveAccountRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    // @Autowired
    // private SpendAndSaveNotificationService notificationService; // TODO: Uncomment when notification methods are implemented
    
    public Optional<SpendAndSaveAccount> getAccountByUser(User user) {
        return spendAndSaveAccountRepository.findByUser(user);
    }
    
    @Transactional
    public SpendAndSaveAccount createAccount(User user) {
        try {
            // Generate unique account number
            String accountNumber = generateAccountNumber();
            
            // Create the account
            SpendAndSaveAccount account = new SpendAndSaveAccount();
            account.setUser(user);
            account.setAccountNumber(accountNumber);
            account.setBalance(BigDecimal.ZERO);
            account.setTotalInterestEarned(BigDecimal.ZERO);
            account.setTotalSavedFromSpending(BigDecimal.ZERO);
            account.setIsActive(false);
            
            account = spendAndSaveAccountRepository.save(account);
            
            // Create default settings
            SpendAndSaveSettings settings = new SpendAndSaveSettings();
            settings.setUser(user);
            settings.setPreferredSavingsPercentage(new BigDecimal("5.00"));
            settings.setMinTransactionThreshold(new BigDecimal("100.00"));
            settings.setDefaultWithdrawalDestination(SpendAndSaveSettings.WithdrawalDestination.WALLET);
            
            spendAndSaveSettingsRepository.save(settings);
            
            log.info("Created Spend and Save account for user {}", user.getUsername());
            return account;
            
        } catch (Exception e) {
            log.error("Error creating Spend and Save account for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    private String generateAccountNumber() {
        Random random = new Random();
        String number;
        do {
            // Generate a 10-digit number starting with '8' to distinguish from regular accounts
            number = "8" + String.format("%09d", random.nextInt(1000000000));
        } while (spendAndSaveAccountRepository.existsByAccountNumber(number));
        return number;
    }
    
    @Transactional
    public SpendAndSaveAccount activateSpendAndSave(User user, BigDecimal savingsPercentage, String fundSource, 
                                                   BigDecimal initialAmount, BigDecimal walletAmount, BigDecimal xySaveAmount) {
        try {
            SpendAndSaveAccount account = spendAndSaveAccountRepository.findByUser(user)
                .orElseGet(() -> createAccount(user));
            
            // Create or update settings
            SpendAndSaveSettings settings = spendAndSaveSettingsRepository.findByUser(user)
                .orElseGet(() -> {
                    SpendAndSaveSettings newSettings = new SpendAndSaveSettings();
                    newSettings.setUser(user);
                    return newSettings;
                });
            
            settings.setPreferredSavingsPercentage(savingsPercentage);
            spendAndSaveSettingsRepository.save(settings);
            
            // Handle initial fund transfer if amount is provided
            if ("both".equals(fundSource)) {
                if ((walletAmount != null && walletAmount.compareTo(BigDecimal.ZERO) > 0) || 
                    (xySaveAmount != null && xySaveAmount.compareTo(BigDecimal.ZERO) > 0)) {
                    transferInitialFunds(user, account, fundSource, BigDecimal.ZERO, walletAmount, xySaveAmount);
                }
            } else if (initialAmount != null && initialAmount.compareTo(BigDecimal.ZERO) > 0) {
                transferInitialFunds(user, account, fundSource, initialAmount, null, null);
            }
            
            // Activate account
            account.activate(savingsPercentage);
            account = spendAndSaveAccountRepository.save(account);
            
            // Send activation notification
            // notificationService.sendAccountActivatedNotification(user, account, savingsPercentage); // TODO: Implement this method
            
            log.info("Activated Spend and Save for user {} with {}% from {}", user.getUsername(), savingsPercentage, fundSource);
            return account;
            
        } catch (Exception e) {
            log.error("Error activating Spend and Save for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    @Transactional
    public void transferInitialFunds(User user, SpendAndSaveAccount account, String fundSource, 
                                   BigDecimal amount, BigDecimal walletAmount, BigDecimal xySaveAmount) {
        try {
            BigDecimal totalTransferred = BigDecimal.ZERO;
            
            if ("wallet".equals(fundSource)) {
                // Transfer from wallet
                Wallet wallet = walletRepository.findByUser(user).stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Wallet not found for user"));
                
                if (wallet.getBalance().compareTo(amount) < 0) {
                    throw new RuntimeException("Insufficient wallet balance");
                }
                
                // Deduct from wallet
                wallet.setBalance(wallet.getBalance().subtract(amount));
                walletRepository.save(wallet);
                
                // Create wallet transaction
                Transaction walletTransaction = new Transaction();
                walletTransaction.setWallet(wallet);
                walletTransaction.setAmount(amount);
                walletTransaction.setType("debit");
                walletTransaction.setStatus("success");
                walletTransaction.setDescription("Initial transfer to Spend and Save account " + account.getAccountNumber());
                walletTransaction.setBalanceAfter(wallet.getBalance());
                walletTransaction.setCreatedAt(LocalDateTime.now());
                
                transactionRepository.save(walletTransaction);
                totalTransferred = amount;
                
            } else if ("xysave".equals(fundSource)) {
                // Transfer from XySave account
                XySaveAccount xySaveAccount = xySaveAccountRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("XySave account not found for user"));
                
                if (xySaveAccount.getBalance().compareTo(amount) < 0) {
                    throw new RuntimeException("Insufficient XySave balance");
                }
                
                // Deduct from XySave account
                xySaveAccount.setBalance(xySaveAccount.getBalance().subtract(amount));
                xySaveAccountRepository.save(xySaveAccount);
                totalTransferred = amount;
                
            } else if ("both".equals(fundSource)) {
                // Transfer from both sources
                if (walletAmount != null && walletAmount.compareTo(BigDecimal.ZERO) > 0) {
                    Wallet wallet = walletRepository.findByUser(user).stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Wallet not found for user"));
                    
                    if (wallet.getBalance().compareTo(walletAmount) < 0) {
                        throw new RuntimeException("Insufficient wallet balance");
                    }
                    
                    wallet.setBalance(wallet.getBalance().subtract(walletAmount));
                    walletRepository.save(wallet);
                    
                    // Create wallet transaction
                    Transaction walletTransaction = new Transaction();
                    walletTransaction.setWallet(wallet);
                    walletTransaction.setAmount(walletAmount);
                    walletTransaction.setType("debit");
                    walletTransaction.setStatus("success");
                    walletTransaction.setDescription("Initial transfer to Spend and Save account " + account.getAccountNumber());
                    walletTransaction.setBalanceAfter(wallet.getBalance());
                    walletTransaction.setCreatedAt(LocalDateTime.now());
                    
                    transactionRepository.save(walletTransaction);
                    totalTransferred = totalTransferred.add(walletAmount);
                }
                
                if (xySaveAmount != null && xySaveAmount.compareTo(BigDecimal.ZERO) > 0) {
                    XySaveAccount xySaveAccount = xySaveAccountRepository.findByUser(user)
                        .orElseThrow(() -> new RuntimeException("XySave account not found for user"));
                    
                    if (xySaveAccount.getBalance().compareTo(xySaveAmount) < 0) {
                        throw new RuntimeException("Insufficient XySave balance");
                    }
                    
                    xySaveAccount.setBalance(xySaveAccount.getBalance().subtract(xySaveAmount));
                    xySaveAccountRepository.save(xySaveAccount);
                    totalTransferred = totalTransferred.add(xySaveAmount);
                }
            }
            
            // Credit to Spend and Save account
            if (totalTransferred.compareTo(BigDecimal.ZERO) > 0) {
                account.setBalance(account.getBalance().add(totalTransferred));
                spendAndSaveAccountRepository.save(account);
                
                // Create Spend and Save transaction
                SpendAndSaveTransaction spendAndSaveTransaction = new SpendAndSaveTransaction();
                spendAndSaveTransaction.setSpendAndSaveAccount(account);
                spendAndSaveTransaction.setTransactionType(SpendAndSaveTransaction.TransactionType.MANUAL_DEPOSIT);
                spendAndSaveTransaction.setAmount(totalTransferred);
                spendAndSaveTransaction.setBalanceBefore(account.getBalance().subtract(totalTransferred));
                spendAndSaveTransaction.setBalanceAfter(account.getBalance());
                spendAndSaveTransaction.setReference("SAS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                spendAndSaveTransaction.setDescription("Initial funding from " + fundSource);
                spendAndSaveTransaction.setCreatedAt(LocalDateTime.now());
                
                spendAndSaveTransactionRepository.save(spendAndSaveTransaction);
            }
            
            log.info("Transferred {} from {} to Spend and Save account {}", totalTransferred, fundSource, account.getAccountNumber());
            
        } catch (Exception e) {
            log.error("Error transferring initial funds from {}: {}", fundSource, e.getMessage());
            throw e;
        }
    }
    
    @Transactional
    public SpendAndSaveAccount deactivateSpendAndSave(User user) {
        try {
            SpendAndSaveAccount account = spendAndSaveAccountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Spend and Save account not found"));
            
            account.deactivate();
            account = spendAndSaveAccountRepository.save(account);
            
            // Send deactivation notification
            // notificationService.sendAccountDeactivatedNotification(user, account); // TODO: Implement this method
            
            log.info("Deactivated Spend and Save for user {}", user.getUsername());
            return account;
            
        } catch (Exception e) {
            log.error("Error deactivating Spend and Save for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    @Transactional
    public SpendAndSaveTransaction processSpendingTransaction(Transaction transactionInstance) {
        log.info("🔍 process_spending_transaction called for transaction {}", transactionInstance.getId());
        log.info("  Transaction type: {}", transactionInstance.getType());
        log.info("  Transaction status: {}", transactionInstance.getStatus());
        log.info("  Transaction amount: {}", transactionInstance.getAmount());
        log.info("  User: {}", transactionInstance.getWallet().getUser().getUsername());
        
        try {
            // Only process debit transactions
            if (!"debit".equals(transactionInstance.getType())) {
                log.info("⏭️ Skipping - not a debit transaction (type: {})", transactionInstance.getType());
                return null;
            }
            
            // Get user's Spend and Save account
            SpendAndSaveAccount account = spendAndSaveAccountRepository.findByUser(transactionInstance.getWallet().getUser())
                .orElse(null);
            
            if (account == null) {
                log.info("❌ No Spend and Save account found for user {}", transactionInstance.getWallet().getUser().getUsername());
                return null;
            }
            
            log.info("✅ Found Spend and Save account: {}", account.getAccountNumber());
            log.info("  Is active: {}", account.getIsActive());
            log.info("  Savings percentage: {}%", account.getSavingsPercentage());
            log.info("  Current balance: {}", account.getBalance());
            
            // Check if Spend and Save is active
            if (!account.getIsActive()) {
                log.info("⏭️ Skipping - Spend and Save account is not active");
                return null;
            }
            
            // Calculate auto-save amount
            log.info("🔢 Calculating auto-save amount...");
            BigDecimal autoSaveAmount = account.processSpendingTransaction(transactionInstance.getAmount());
            log.info("  Calculated auto-save amount: {}", autoSaveAmount);
            
            if (autoSaveAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.info("⏭️ Skipping - auto-save amount is 0 or negative");
                return null;
            }
            
            log.info("✅ Proceeding with auto-save of {}", autoSaveAmount);
            
            // Determine funding source
            String fundingSource = determineFundingSource(account, transactionInstance);
            
            // Deduct from appropriate source
            if ("xysave".equals(fundingSource)) {
                XySaveAccount xySaveAccount = xySaveAccountRepository.findByUser(account.getUser())
                    .orElseThrow(() -> new RuntimeException("XySave account not found"));
                
                if (xySaveAccount.getBalance().compareTo(autoSaveAmount) < 0) {
                    log.warn("Insufficient XySave balance for auto-save. Required: {}, Available: {}", 
                               autoSaveAmount, xySaveAccount.getBalance());
                    return null;
                }
                
                xySaveAccount.setBalance(xySaveAccount.getBalance().subtract(autoSaveAmount));
                xySaveAccountRepository.save(xySaveAccount);
                log.info("  Deducted {} from XySave. New balance: {}", autoSaveAmount, xySaveAccount.getBalance());
                
            } else {
                // Deduct from wallet
                Wallet wallet = transactionInstance.getWallet();
                if (wallet.getBalance().compareTo(autoSaveAmount) < 0) {
                    log.warn("Insufficient wallet balance for auto-save. Required: {}, Available: {}", 
                               autoSaveAmount, wallet.getBalance());
                    return null;
                }
                
                wallet.setBalance(wallet.getBalance().subtract(autoSaveAmount));
                walletRepository.save(wallet);
                log.info("  Deducted {} from wallet. New balance: {}", autoSaveAmount, wallet.getBalance());
            }
            
            // Create auto-save transaction
            SpendAndSaveTransaction autoSaveTx = new SpendAndSaveTransaction();
            autoSaveTx.setSpendAndSaveAccount(account);
            autoSaveTx.setTransactionType(SpendAndSaveTransaction.TransactionType.AUTO_SAVE);
            autoSaveTx.setAmount(autoSaveAmount);
            autoSaveTx.setBalanceBefore(account.getBalance());
            autoSaveTx.setBalanceAfter(account.getBalance().add(autoSaveAmount));
            autoSaveTx.setReference(UUID.randomUUID().toString());
            autoSaveTx.setDescription("Auto-save from spending transaction " + transactionInstance.getReference());
            autoSaveTx.setOriginalTransactionId(transactionInstance.getId());
            autoSaveTx.setOriginalTransactionAmount(transactionInstance.getAmount());
            autoSaveTx.setSavingsPercentageApplied(account.getSavingsPercentage());
            autoSaveTx.setCreatedAt(LocalDateTime.now());
            
            spendAndSaveTransactionRepository.save(autoSaveTx);
            
            // Update account balance
            account.setBalance(account.getBalance().add(autoSaveAmount));
            account.setTotalSavedFromSpending(account.getTotalSavedFromSpending().add(autoSaveAmount));
            account.setTotalTransactionsProcessed(account.getTotalTransactionsProcessed() + 1);
            account.setLastAutoSaveDate(LocalDate.now());
            spendAndSaveAccountRepository.save(account);
            
            // Send spending save notification
            // notificationService.sendSpendingSaveNotification(
            //     account.getUser(), account, transactionInstance.getAmount(), 
            //     autoSaveAmount, account.getTotalSavedFromSpending()
            
            log.info("✅ Successfully processed auto-save of {} for user {}", autoSaveAmount, account.getUser().getUsername());
            return autoSaveTx;
            
        } catch (Exception e) {
            log.error("❌ Error processing spending transaction for auto-save: {}", e.getMessage());
            return null;
        }
    }
    
    private String determineFundingSource(SpendAndSaveAccount account, Transaction transactionInstance) {
        try {
            // Load user settings
            SpendAndSaveSettings settings = spendAndSaveSettingsRepository.findByUser(account.getUser())
                .orElse(new SpendAndSaveSettings());
            
            String fundingPreference = settings.getFundingPreference().getCode();
            if (fundingPreference == null) {
                fundingPreference = "auto";
            }
            
            // Check if XySave account exists and has sufficient balance
            Optional<XySaveAccount> xySaveAccountOpt = xySaveAccountRepository.findByUser(account.getUser());
            boolean canUseXySave = xySaveAccountOpt.isPresent() && 
                                 xySaveAccountOpt.get().getIsActive() &&
                                 xySaveAccountOpt.get().getBalance().compareTo(account.getMinTransactionAmount()) >= 0;
            
            if ("xysave".equals(fundingPreference) && canUseXySave) {
                return "xysave";
            } else if ("wallet".equals(fundingPreference)) {
                return "wallet";
            } else if ("auto".equals(fundingPreference) && canUseXySave) {
                return "xysave";
            } else {
                return "wallet";
            }
            
        } catch (Exception e) {
            log.warn("Error determining funding source, defaulting to wallet: {}", e.getMessage());
            return "wallet";
        }
    }
    
    @Transactional
    public SpendAndSaveTransaction withdrawFromSpendAndSave(User user, BigDecimal amount, String destination) {
        try {
            SpendAndSaveAccount account = spendAndSaveAccountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Spend and Save account not found"));
            
            if (!account.canWithdraw(amount)) {
                throw new RuntimeException("Insufficient balance or account not active");
            }
            
            // Create withdrawal transaction
            SpendAndSaveTransaction withdrawalTx = new SpendAndSaveTransaction();
            withdrawalTx.setSpendAndSaveAccount(account);
            withdrawalTx.setTransactionType(SpendAndSaveTransaction.TransactionType.WITHDRAWAL);
            withdrawalTx.setAmount(amount);
            withdrawalTx.setBalanceBefore(account.getBalance());
            withdrawalTx.setBalanceAfter(account.getBalance().subtract(amount));
            withdrawalTx.setReference(UUID.randomUUID().toString());
            withdrawalTx.setDescription("Withdrawal to " + destination);
            withdrawalTx.setWithdrawalDestination(SpendAndSaveTransaction.WithdrawalDestination.valueOf(destination.toUpperCase()));
            withdrawalTx.setDestinationAccount(destination);
            withdrawalTx.setCreatedAt(LocalDateTime.now());
            
            spendAndSaveTransactionRepository.save(withdrawalTx);
            
            // Update account balance
            account.setBalance(account.getBalance().subtract(amount));
            spendAndSaveAccountRepository.save(account);
            
            // Transfer to destination
            if ("wallet".equals(destination)) {
                Wallet wallet = walletRepository.findByUser(user).stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Wallet not found"));
                
                wallet.setBalance(wallet.getBalance().add(amount));
                walletRepository.save(wallet);
                
                // Create wallet transaction
                Transaction walletTransaction = new Transaction();
                walletTransaction.setWallet(wallet);
                walletTransaction.setType("credit");
                walletTransaction.setChannel("transfer");
                walletTransaction.setAmount(amount);
                walletTransaction.setDescription("Withdrawal from Spend and Save account");
                walletTransaction.setStatus("success");
                walletTransaction.setBalanceAfter(wallet.getBalance());
                walletTransaction.setCreatedAt(LocalDateTime.now());
                
                transactionRepository.save(walletTransaction);
                
            } else if ("xysave".equals(destination)) {
                XySaveAccount xySaveAccount = xySaveAccountRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("XySave account not found"));
                
                xySaveAccount.setBalance(xySaveAccount.getBalance().add(amount));
                xySaveAccountRepository.save(xySaveAccount);
            }
            
            // Send withdrawal notification
            // notificationService.sendWithdrawalNotification(user, account, amount, destination); // TODO: Implement this method
            
            log.info("Withdrew {} from Spend and Save account for user {}", amount, user.getUsername());
            return withdrawalTx;
            
        } catch (Exception e) {
            log.error("Error withdrawing from Spend and Save for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    @Transactional
    public SpendAndSaveTransaction calculateAndCreditInterest(User user) {
        try {
            SpendAndSaveAccount account = spendAndSaveAccountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Spend and Save account not found"));
            
            if (account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            
            // Calculate interest using tiered rates
            BigDecimal interestAmount = account.calculateTieredInterest();
            String interestBreakdown = account.getInterestBreakdown();
            
            if (interestAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            
            // Create interest credit transaction
            SpendAndSaveTransaction interestTx = new SpendAndSaveTransaction();
            interestTx.setSpendAndSaveAccount(account);
            interestTx.setTransactionType(SpendAndSaveTransaction.TransactionType.INTEREST_CREDIT);
            interestTx.setAmount(interestAmount);
            interestTx.setBalanceBefore(account.getBalance());
            interestTx.setBalanceAfter(account.getBalance().add(interestAmount));
            interestTx.setReference(UUID.randomUUID().toString());
            interestTx.setDescription("Daily interest credit");
            interestTx.setInterestEarned(interestAmount);
            interestTx.setInterestBreakdown(interestBreakdown);
            interestTx.setCreatedAt(LocalDateTime.now());
            
            spendAndSaveTransactionRepository.save(interestTx);
            
            // Update account
            account.setBalance(account.getBalance().add(interestAmount));
            account.setTotalInterestEarned(account.getTotalInterestEarned().add(interestAmount));
            account.setLastInterestCalculation(LocalDateTime.now());
            spendAndSaveAccountRepository.save(account);
            
            // Send interest credited notification
            // notificationService.sendInterestCreditedNotification(
            //     user, account, interestAmount, account.getTotalInterestEarned()
            // ); // TODO: Implement this method
            
            log.info("Credited {} interest to Spend and Save account for user {}", interestAmount, user.getUsername());
            return interestTx;
            
        } catch (Exception e) {
            log.error("Error calculating interest for user {}: {}", user.getUsername(), e.getMessage());
            return null;
        }
    }
    
    public Map<String, Object> getAccountSummary(User user) {
        try {
            SpendAndSaveAccount account = spendAndSaveAccountRepository.findByUser(user)
                .orElse(null);
            
            if (account == null) {
                return null;
            }
            
            SpendAndSaveSettings settings = spendAndSaveSettingsRepository.findByUser(user)
                .orElse(new SpendAndSaveSettings());
            
            // Calculate current interest breakdown
            String interestBreakdown = account.getInterestBreakdown();
            
            // Get recent transactions
            List<SpendAndSaveTransaction> recentTransactions = spendAndSaveTransactionRepository
                .findBySpendAndSaveAccountOrderByCreatedAtDesc(account, 10);
            
            Map<String, Object> summary = new HashMap<>();
            Map<String, Object> accountData = new HashMap<>();
            accountData.put("account_number", account.getAccountNumber());
            accountData.put("balance", account.getBalance());
            accountData.put("is_active", account.getIsActive());
            accountData.put("savings_percentage", account.getSavingsPercentage());
            accountData.put("total_interest_earned", account.getTotalInterestEarned());
            accountData.put("total_saved_from_spending", account.getTotalSavedFromSpending());
            accountData.put("total_transactions_processed", account.getTotalTransactionsProcessed());
            accountData.put("last_auto_save_date", account.getLastAutoSaveDate());
            accountData.put("default_withdrawal_destination", account.getDefaultWithdrawalDestination());
            accountData.put("created_at", account.getCreatedAt());
            accountData.put("updated_at", account.getUpdatedAt());
            summary.put("account", accountData);
            
            Map<String, Object> settingsData = new HashMap<>();
            settingsData.put("auto_save_notifications", settings.getAutoSaveNotifications());
            settingsData.put("interest_notifications", settings.getInterestNotifications());
            settingsData.put("withdrawal_notifications", settings.getWithdrawalNotifications());
            settingsData.put("preferred_savings_percentage", settings.getPreferredSavingsPercentage());
            settingsData.put("min_transaction_threshold", settings.getMinTransactionThreshold());
            settingsData.put("default_withdrawal_destination", settings.getDefaultWithdrawalDestination());
            settingsData.put("interest_payout_frequency", settings.getInterestPayoutFrequency());
            summary.put("settings", settingsData);
            
            summary.put("interest_breakdown", interestBreakdown);
            List<Map<String, Object>> transactionData = recentTransactions.stream()
                .map(tx -> {
                    Map<String, Object> txData = new HashMap<>();
                    txData.put("id", tx.getId());
                    txData.put("transaction_type", tx.getTransactionType());
                    txData.put("amount", tx.getAmount());
                    txData.put("description", tx.getDescription());
                    txData.put("created_at", tx.getCreatedAt());
                    txData.put("balance_after", tx.getBalanceAfter());
                    return txData;
                })
                .toList();
            summary.put("recent_transactions", transactionData);
            
            return summary;
            
        } catch (Exception e) {
            log.error("Error getting account summary for user {}: {}", user.getUsername(), e.getMessage());
            return null;
        }
    }
    
    @Transactional
    public SpendAndSaveSettings updateSettings(User user, Map<String, Object> settingsData) {
        try {
            SpendAndSaveSettings settings = spendAndSaveSettingsRepository.findByUser(user)
                .orElseGet(() -> {
                    SpendAndSaveSettings newSettings = new SpendAndSaveSettings();
                    newSettings.setUser(user);
                    return newSettings;
                });
            
            // Update settings based on provided data
            if (settingsData.containsKey("auto_save_notifications")) {
                settings.setAutoSaveNotifications((Boolean) settingsData.get("auto_save_notifications"));
            }
            if (settingsData.containsKey("interest_notifications")) {
                settings.setInterestNotifications((Boolean) settingsData.get("interest_notifications"));
            }
            if (settingsData.containsKey("withdrawal_notifications")) {
                settings.setWithdrawalNotifications((Boolean) settingsData.get("withdrawal_notifications"));
            }
            if (settingsData.containsKey("preferred_savings_percentage")) {
                settings.setPreferredSavingsPercentage((BigDecimal) settingsData.get("preferred_savings_percentage"));
            }
            if (settingsData.containsKey("min_transaction_threshold")) {
                settings.setMinTransactionThreshold((BigDecimal) settingsData.get("min_transaction_threshold"));
            }
            if (settingsData.containsKey("default_withdrawal_destination")) {
                String destination = (String) settingsData.get("default_withdrawal_destination");
                settings.setDefaultWithdrawalDestination(SpendAndSaveSettings.WithdrawalDestination.valueOf(destination.toUpperCase()));
            }
            if (settingsData.containsKey("funding_preference")) {
                String preference = (String) settingsData.get("funding_preference");
                settings.setFundingPreference(SpendAndSaveSettings.FundingPreference.valueOf(preference.toUpperCase()));
            }
            if (settingsData.containsKey("auto_withdrawal_enabled")) {
                settings.setAutoWithdrawalEnabled((Boolean) settingsData.get("auto_withdrawal_enabled"));
            }
            if (settingsData.containsKey("auto_withdrawal_threshold")) {
                settings.setAutoWithdrawalThreshold((BigDecimal) settingsData.get("auto_withdrawal_threshold"));
            }
            
            settings = spendAndSaveSettingsRepository.save(settings);
            
            log.info("Updated Spend and Save settings for user {}", user.getUsername());
            return settings;
            
        } catch (Exception e) {
            log.error("Error updating settings for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
}