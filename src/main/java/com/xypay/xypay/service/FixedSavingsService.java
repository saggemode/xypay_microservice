package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.dto.*;
import com.xypay.xypay.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FixedSavingsService {
    
    private static final Logger logger = LoggerFactory.getLogger(FixedSavingsService.class);
    
    @Autowired
    private FixedSavingsAccountRepository fixedSavingsAccountRepository;
    
    @Autowired
    private FixedSavingsSettingsRepository fixedSavingsSettingsRepository;
    
    @Autowired
    private FixedSavingsTransactionRepository fixedSavingsTransactionRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private XySaveAccountRepository xySaveAccountRepository;
    
    @Autowired
    private FixedSavingsNotificationService fixedSavingsNotificationService;
    
    @Autowired
    private FixedSavingsMapper fixedSavingsMapper;
    
    /**
     * Create a new fixed savings account
     */
    public FixedSavingsAccount createFixedSavings(User user, BigDecimal amount, FixedSavingsSource source, 
                                                 FixedSavingsPurpose purpose, String purposeDescription,
                                                 LocalDate startDate, LocalDate paybackDate, 
                                                 Boolean autoRenewalEnabled) {
        try {
            // Validate user has sufficient funds
            if (!validateSufficientFunds(user, amount, source)) {
                throw new RuntimeException("Insufficient funds for fixed savings");
            }
            
            // Create fixed savings account
            FixedSavingsAccount fixedSavings = new FixedSavingsAccount();
            fixedSavings.setUser(user);
            fixedSavings.setAmount(amount);
            fixedSavings.setSource(source);
            fixedSavings.setPurpose(purpose);
            fixedSavings.setPurposeDescription(purposeDescription);
            fixedSavings.setStartDate(startDate);
            fixedSavings.setPaybackDate(paybackDate);
            fixedSavings.setAutoRenewalEnabled(autoRenewalEnabled != null ? autoRenewalEnabled : false);
            
            // Save the account (triggers PrePersist to calculate rates)
            fixedSavings = fixedSavingsAccountRepository.save(fixedSavings);
            
            // Deduct funds from source accounts
            deductFunds(user, amount, source);
            
            // Create initial transaction
            FixedSavingsTransaction transaction = new FixedSavingsTransaction();
            transaction.setFixedSavingsAccount(fixedSavings);
            transaction.setTransactionType(FixedSavingsTransaction.TransactionType.INITIAL_DEPOSIT);
            transaction.setAmount(amount);
            transaction.setBalanceBefore(amount);
            transaction.setBalanceAfter(amount);
            transaction.setReference("FS_INIT_" + fixedSavings.getId());
            transaction.setDescription("Initial fixed savings deposit - " + 
                (purposeDescription != null ? purposeDescription : purpose.getDescription()));
            transaction.setSourceAccount(FixedSavingsTransaction.Source.valueOf(source.name()));
            transaction.setInterestRateApplied(fixedSavings.getInterestRate());
            fixedSavingsTransactionRepository.save(transaction);
            
            // Send notifications
            fixedSavingsNotificationService.sendFixedSavingsCreatedNotification(fixedSavings);
            
            return fixedSavings;
            
        } catch (Exception e) {
            logger.error("Error creating fixed savings for user {}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Error creating fixed savings", e);
        }
    }
    
    /**
     * Validate user has sufficient funds for fixed savings
     */
    private boolean validateSufficientFunds(User user, BigDecimal amount, FixedSavingsSource source) {
        try {
            List<Wallet> walletList = walletRepository.findByUser(user);
            if (walletList.isEmpty()) {
                logger.warn("User {} has no wallet", user.getId());
                return false;
            }
            Wallet wallet = walletList.get(0);
            
            switch (source) {
                case WALLET:
                    return wallet.getBalance().compareTo(amount) >= 0;
                case XYSAVE:
                    Optional<XySaveAccount> xysaveAccountOpt = xySaveAccountRepository.findByUser(user);
                    if (xysaveAccountOpt.isEmpty()) {
                        logger.warn("User {} has no XySave account for XySave-only fixed savings", user.getId());
                        return false;
                    }
                    XySaveAccount xysaveAccount = xysaveAccountOpt.get();
                    return xysaveAccount.getBalance().compareTo(amount) >= 0;
                case BOTH:
                    Optional<XySaveAccount> xysaveOpt = xySaveAccountRepository.findByUser(user);
                    if (xysaveOpt.isEmpty()) {
                        logger.warn("User {} has no XySave account for BOTH source fixed savings", user.getId());
                        return wallet.getBalance().compareTo(amount) >= 0;
                    }
                    XySaveAccount xysave = xysaveOpt.get();
                    BigDecimal combined = wallet.getBalance().add(xysave.getBalance());
                    return combined.compareTo(amount) >= 0;
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error validating funds for user {}: {}", user.getId(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Deduct funds from source accounts
     */
    private void deductFunds(User user, BigDecimal amount, FixedSavingsSource source) {
        try {
            List<Wallet> walletList = walletRepository.findByUser(user);
            if (walletList.isEmpty()) {
                throw new RuntimeException("User has no wallet");
            }
            Wallet wallet = walletList.get(0);
            
            switch (source) {
                case WALLET:
                    wallet.setBalance(wallet.getBalance().subtract(amount));
                    walletRepository.save(wallet);
                    break;
                case XYSAVE:
                    Optional<XySaveAccount> xysaveAccountOpt = xySaveAccountRepository.findByUser(user);
                    if (xysaveAccountOpt.isEmpty()) {
                        throw new RuntimeException("XySave account not found");
                    }
                    XySaveAccount xysaveAccount = xysaveAccountOpt.get();
                    xysaveAccount.setBalance(xysaveAccount.getBalance().subtract(amount));
                    xySaveAccountRepository.save(xysaveAccount);
                    break;
                case BOTH:
                    Optional<XySaveAccount> xysaveOpt = xySaveAccountRepository.findByUser(user);
                    if (xysaveOpt.isEmpty()) {
                        logger.warn("User {} has no XySave account, deducting full amount from wallet", user.getId());
                        wallet.setBalance(wallet.getBalance().subtract(amount));
                        walletRepository.save(wallet);
                        break;
                    }
                    XySaveAccount xysave = xysaveOpt.get();
                    
                    // Flexible deduction: draw from wallet first, then XySave to cover the rest
                    BigDecimal remaining = amount;
                    if (wallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal walletDeduction = wallet.getBalance().min(remaining);
                        if (walletDeduction.compareTo(BigDecimal.ZERO) > 0) {
                            wallet.setBalance(wallet.getBalance().subtract(walletDeduction));
                            remaining = remaining.subtract(walletDeduction);
                        }
                    }
                    if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                        xysave.setBalance(xysave.getBalance().subtract(remaining));
                        xySaveAccountRepository.save(xysave);
                    }
                    walletRepository.save(wallet);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error deducting funds for user {}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Error deducting funds", e);
        }
    }
    
    /**
     * Process maturity payout for fixed savings
     */
    public boolean processMaturityPayout(FixedSavingsAccount fixedSavings) {
        try {
            if (!fixedSavings.canBePaidOut()) {
                throw new RuntimeException("Fixed savings cannot be paid out");
            }
            
            // Mark as matured if not already
            if (!fixedSavings.getIsMatured()) {
                fixedSavings.markAsMatured();
                fixedSavingsAccountRepository.save(fixedSavings);
            }
            
            // Pay out to xysave account
            boolean success = payOut(fixedSavings);
            if (success) {
                // Send notifications
                fixedSavingsNotificationService.sendFixedSavingsMaturedNotification(fixedSavings);
                fixedSavingsNotificationService.sendFixedSavingsPaidOutNotification(fixedSavings);
                return true;
            }
            return false;
            
        } catch (Exception e) {
            logger.error("Error processing maturity payout for fixed savings {}: {}", 
                fixedSavings.getId(), e.getMessage());
            throw new RuntimeException("Error processing maturity payout", e);
        }
    }
    
    /**
     * Pay out the matured amount to user's xysave account
     */
    private boolean payOut(FixedSavingsAccount fixedSavings) {
        if (!fixedSavings.canBePaidOut()) {
            return false;
        }
        
        try {
            // Get user's xysave account
            Optional<XySaveAccount> xysaveAccountOpt = xySaveAccountRepository.findByUser(fixedSavings.getUser());
            if (xysaveAccountOpt.isEmpty()) {
                return false;
            }
            XySaveAccount xysaveAccount = xysaveAccountOpt.get();
            
            // Credit the maturity amount
            BigDecimal balanceBefore = xysaveAccount.getBalance();
            xysaveAccount.setBalance(xysaveAccount.getBalance().add(fixedSavings.getMaturityAmount()));
            xySaveAccountRepository.save(xysaveAccount);
            
            // Create transaction record
            FixedSavingsTransaction transaction = new FixedSavingsTransaction();
            transaction.setFixedSavingsAccount(fixedSavings);
            transaction.setTransactionType(FixedSavingsTransaction.TransactionType.MATURITY_PAYOUT);
            transaction.setAmount(fixedSavings.getMaturityAmount());
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(xysaveAccount.getBalance());
            transaction.setReference("FS_PAYOUT_" + fixedSavings.getId());
            transaction.setDescription("Fixed savings maturity payout - " + 
                (fixedSavings.getPurposeDescription() != null ? 
                    fixedSavings.getPurposeDescription() : 
                    fixedSavings.getPurpose().getDescription()));
            transaction.setInterestEarned(fixedSavings.getTotalInterestEarned());
            fixedSavingsTransactionRepository.save(transaction);
            
            // Mark as paid out
            fixedSavings.setIsPaidOut(true);
            fixedSavings.setPaidOutAt(LocalDateTime.now());
            fixedSavingsAccountRepository.save(fixedSavings);
            
            return true;
        } catch (Exception e) {
            logger.error("Error paying out fixed savings {}: {}", fixedSavings.getId(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Process auto-renewal for fixed savings
     */
    public FixedSavingsAccount processAutoRenewal(FixedSavingsAccount fixedSavings) {
        try {
            if (!fixedSavings.getAutoRenewalEnabled() || !fixedSavings.isMature()) {
                return null;
            }
            
            // Calculate new dates
            int durationDays = fixedSavings.getDurationDays();
            LocalDate newStartDate = fixedSavings.getPaybackDate();
            LocalDate newPaybackDate = newStartDate.plusDays(durationDays);
            
            // Create new fixed savings account
            FixedSavingsAccount newFixedSavings = new FixedSavingsAccount();
            newFixedSavings.setUser(fixedSavings.getUser());
            newFixedSavings.setAmount(fixedSavings.getMaturityAmount());
            newFixedSavings.setSource(FixedSavingsSource.XYSAVE); // From xysave since that's where payout goes
            newFixedSavings.setPurpose(fixedSavings.getPurpose());
            newFixedSavings.setPurposeDescription("Auto-renewal of " + 
                (fixedSavings.getPurposeDescription() != null ? 
                    fixedSavings.getPurposeDescription() : 
                    fixedSavings.getPurpose().getDescription()));
            newFixedSavings.setStartDate(newStartDate);
            newFixedSavings.setPaybackDate(newPaybackDate);
            newFixedSavings.setAutoRenewalEnabled(fixedSavings.getAutoRenewalEnabled());
            
            // Save the new account
            newFixedSavings = fixedSavingsAccountRepository.save(newFixedSavings);
            
            // Create auto-renewal transaction
            FixedSavingsTransaction transaction = new FixedSavingsTransaction();
            transaction.setFixedSavingsAccount(newFixedSavings);
            transaction.setTransactionType(FixedSavingsTransaction.TransactionType.AUTO_RENEWAL);
            transaction.setAmount(newFixedSavings.getAmount());
            transaction.setBalanceBefore(newFixedSavings.getAmount());
            transaction.setBalanceAfter(newFixedSavings.getAmount());
            transaction.setReference("FS_RENEWAL_" + newFixedSavings.getId());
            transaction.setDescription("Auto-renewal of fixed savings - " + newFixedSavings.getPurposeDescription());
            transaction.setInterestRateApplied(newFixedSavings.getInterestRate());
            fixedSavingsTransactionRepository.save(transaction);
            
            // Send notification
            fixedSavingsNotificationService.sendFixedSavingsAutoRenewalNotification(newFixedSavings);
            
            return newFixedSavings;
            
        } catch (Exception e) {
            logger.error("Error processing auto-renewal for fixed savings {}: {}", 
                fixedSavings.getId(), e.getMessage());
            throw new RuntimeException("Error processing auto-renewal", e);
        }
    }
    
    /**
     * Get all fixed savings accounts for a user
     */
    @Transactional(readOnly = true)
    public List<FixedSavingsAccountDTO> getUserFixedSavingsAccounts(User user) {
        List<FixedSavingsAccount> accounts = fixedSavingsAccountRepository.findByUser(user);
        return accounts.stream().map(fixedSavingsMapper::toDTO).toList();
    }
    
    /**
     * Get a specific fixed savings account detail
     */
    @Transactional(readOnly = true)
    public FixedSavingsAccountDetailDTO getFixedSavingsAccountDetail(UUID accountId, User user) {
        FixedSavingsAccount account = fixedSavingsAccountRepository.findByIdAndUser(accountId, user);
        return fixedSavingsMapper.toDetailDTO(account);
    }
    
    /**
     * Get fixed savings settings for a user
     */
    @Transactional(readOnly = true)
    public FixedSavingsSettingsDTO getFixedSavingsSettings(User user) {
        List<FixedSavingsSettings> settingsList = fixedSavingsSettingsRepository.findByUser(user);
        FixedSavingsSettings settings = settingsList.isEmpty() ? null : settingsList.get(0);
        return fixedSavingsMapper.toDTO(settings);
    }
    
    /**
     * Update fixed savings settings for a user
     */
    public FixedSavingsSettingsDTO updateFixedSavingsSettings(FixedSavingsSettingsDTO settingsDTO, User user) {
        List<FixedSavingsSettings> settingsList = fixedSavingsSettingsRepository.findByUser(user);
        FixedSavingsSettings settings = settingsList.isEmpty() ? null : settingsList.get(0);
        if (settings == null) {
            settings = new FixedSavingsSettings();
            settings.setUser(user);
        }
        
        settings.setMaturityNotifications(settingsDTO.getMaturityNotifications());
        settings.setInterestNotifications(settingsDTO.getInterestNotifications());
        settings.setAutoRenewalNotifications(settingsDTO.getAutoRenewalNotifications());
        settings.setDefaultAutoRenewal(settingsDTO.getDefaultAutoRenewal());
        settings.setDefaultRenewalDuration(settingsDTO.getDefaultRenewalDuration());
        
        // Convert source string back to enum
        if (settingsDTO.getDefaultSource() != null) {
            try {
                FixedSavingsSettings.Source source = FixedSavingsSettings.Source.valueOf(settingsDTO.getDefaultSource());
                settings.setDefaultSource(source);
            } catch (IllegalArgumentException e) {
                // Handle invalid source value
            }
        }
        
        FixedSavingsSettings savedSettings = fixedSavingsSettingsRepository.save(settings);
        return fixedSavingsMapper.toDTO(savedSettings);
    }
    
    /**
     * Calculate interest rate information
     */
    public FixedSavingsInterestRateDTO calculateInterestRate(FixedSavingsInterestRateDTO rateDTO) {
        FixedSavingsAccount tempAccount = new FixedSavingsAccount();
        tempAccount.setAmount(rateDTO.getAmount());
        tempAccount.setStartDate(rateDTO.getStartDate());
        tempAccount.setPaybackDate(rateDTO.getPaybackDate());
        
        BigDecimal interestRate = tempAccount.calculateInterestRate();
        BigDecimal maturityAmount = tempAccount.calculateMaturityAmount();
        BigDecimal interestEarned = maturityAmount.subtract(tempAccount.getAmount());
        Integer durationDays = tempAccount.getDurationDays();
        
        rateDTO.setInterestRate(interestRate);
        rateDTO.setMaturityAmount("₦" + maturityAmount.toPlainString());
        rateDTO.setInterestEarned("₦" + interestEarned.toPlainString());
        rateDTO.setDurationDays(durationDays);
        
        return rateDTO;
    }
    
    /**
     * Get fixed savings choices (purposes and sources)
     */
    public FixedSavingsChoicesDTO getFixedSavingsChoices() {
        FixedSavingsChoicesDTO choices = new FixedSavingsChoicesDTO();
        choices.setPurposes(fixedSavingsMapper.getPurposeChoices());
        choices.setSources(fixedSavingsMapper.getSourceChoices());
        return choices;
    }
    
    /**
     * Get summary of user's fixed savings
     */
    @Transactional(readOnly = true)
    public FixedSavingsSummaryDTO getUserFixedSavingsSummary(User user) {
        try {
            List<FixedSavingsAccount> activeFixedSavings = fixedSavingsAccountRepository
                .findByUserAndIsActiveTrue(user);
            
            BigDecimal totalActiveAmount = BigDecimal.ZERO;
            BigDecimal totalMaturityAmount = BigDecimal.ZERO;
            BigDecimal totalInterestEarned = BigDecimal.ZERO;
            
            for (FixedSavingsAccount account : activeFixedSavings) {
                totalActiveAmount = totalActiveAmount.add(account.getAmount() != null ? account.getAmount() : BigDecimal.ZERO);
                totalMaturityAmount = totalMaturityAmount.add(account.getMaturityAmount() != null ? account.getMaturityAmount() : BigDecimal.ZERO);
                totalInterestEarned = totalInterestEarned.add(account.getTotalInterestEarned() != null ? account.getTotalInterestEarned() : BigDecimal.ZERO);
            }
            
            List<FixedSavingsAccount> maturedFixedSavings = fixedSavingsAccountRepository
                .findByUserAndIsMaturedTrueAndIsPaidOutFalse(user);
            
            BigDecimal maturedUnpaidAmount = BigDecimal.ZERO;
            for (FixedSavingsAccount account : maturedFixedSavings) {
                maturedUnpaidAmount = maturedUnpaidAmount.add(account.getMaturityAmount() != null ? account.getMaturityAmount() : BigDecimal.ZERO);
            }
            
            FixedSavingsSummaryDTO summary = new FixedSavingsSummaryDTO();
            summary.setTotalActiveFixedSavings(activeFixedSavings.size());
            summary.setTotalActiveAmount("₦" + totalActiveAmount.toPlainString());
            summary.setTotalMaturityAmount("₦" + totalMaturityAmount.toPlainString());
            summary.setTotalInterestEarned("₦" + totalInterestEarned.toPlainString());
            summary.setMaturedUnpaidCount(maturedFixedSavings.size());
            summary.setMaturedUnpaidAmount(maturedUnpaidAmount);
            
            return summary;
        } catch (Exception e) {
            logger.error("Error getting fixed savings summary for user {}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Error getting fixed savings summary", e);
        }
    }
    
    /**
     * Get fixed savings account by ID and user
     */
    @Transactional(readOnly = true)
    public FixedSavingsAccount getFixedSavingsAccountByIdAndUser(UUID accountId, User user) {
        return fixedSavingsAccountRepository.findByIdAndUser(accountId, user);
    }
    
    /**
     * Send maturity reminder notification
     */
    public void sendMaturityReminderNotification(FixedSavingsAccount account) {
        fixedSavingsNotificationService.sendFixedSavingsMaturityReminderNotification(account);
    }
}