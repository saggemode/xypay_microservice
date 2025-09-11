package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.dto.*;
import com.xypay.xypay.repository.FixedSavingsAccountRepository;
import com.xypay.xypay.repository.FixedSavingsSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FixedSavingsService {
    
    private static final Logger logger = LoggerFactory.getLogger(FixedSavingsService.class);
    
    @Autowired
    private FixedSavingsAccountRepository fixedSavingsAccountRepository;
    
    @Autowired
    private FixedSavingsSettingsRepository fixedSavingsSettingsRepository;
    
    @Autowired
    private FixedSavingsNotificationService fixedSavingsNotificationService;
    
    @Autowired
    private FixedSavingsMapper fixedSavingsMapper;
    
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
    public FixedSavingsAccountDetailDTO getFixedSavingsAccountDetail(Long accountId, User user) {
        FixedSavingsAccount account = fixedSavingsAccountRepository.findByIdAndUser(accountId, user);
        return fixedSavingsMapper.toDetailDTO(account);
    }
    
    /**
     * Create a new fixed savings account
     */
    public FixedSavingsAccount createFixedSavingsAccount(FixedSavingsAccountCreateDTO createDTO, User user) {
        try {
            // Validate user has sufficient funds
            if (!_validateSufficientFunds(user, createDTO.getAmount(), createDTO.getSource())) {
                throw new RuntimeException("Insufficient funds for fixed savings");
            }
            
            // Create fixed savings account
            FixedSavingsAccount fixedSavings = new FixedSavingsAccount();
            fixedSavings.setUser(user);
            fixedSavings.setAmount(createDTO.getAmount());
            fixedSavings.setSource(createDTO.getSource());
            fixedSavings.setPurpose(createDTO.getPurpose());
            fixedSavings.setPurposeDescription(createDTO.getPurposeDescription());
            fixedSavings.setStartDate(createDTO.getStartDate());
            fixedSavings.setPaybackDate(createDTO.getPaybackDate());
            fixedSavings.setAutoRenewalEnabled(createDTO.getAutoRenewalEnabled());
            
            // Calculate interest rate
            fixedSavings.setInterestRate(fixedSavings.calculateInterestRate());
            
            // Calculate maturity amount
            fixedSavings.setMaturityAmount(fixedSavings.calculateMaturityAmount());
            
            // Generate account number
            fixedSavings.setAccountNumber(generateAccountNumber());
            
            // Save the account
            fixedSavings = fixedSavingsAccountRepository.save(fixedSavings);
            
            // Deduct funds from source accounts
            _deductFunds(user, createDTO.getAmount(), createDTO.getSource());
            
            // Create initial transaction
            FixedSavingsTransaction transaction = new FixedSavingsTransaction();
            transaction.setFixedSavingsAccount(fixedSavings);
            transaction.setTransactionType(FixedSavingsTransaction.TransactionType.INITIAL_DEPOSIT);
            transaction.setAmount(createDTO.getAmount());
            transaction.setBalanceBefore(createDTO.getAmount());
            transaction.setBalanceAfter(createDTO.getAmount());
            transaction.setReference("FS_INIT_" + fixedSavings.getId());
            transaction.setDescription("Initial fixed savings deposit - " + 
                (createDTO.getPurposeDescription() != null ? createDTO.getPurposeDescription() : ""));
            transaction.setSourceAccount(FixedSavingsTransaction.Source.valueOf(createDTO.getSource().toUpperCase()));
            transaction.setInterestRateApplied(fixedSavings.getInterestRate());
            transaction.setCreatedAt(LocalDateTime.now());
            // Transaction would be saved in a real implementation
            
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
    private boolean _validateSufficientFunds(User user, BigDecimal amount, String source) {
        try {
            // In a real implementation, you would check the user's wallet and xysave account
            // For now, we'll just return true as a placeholder
            return true;
        } catch (Exception e) {
            logger.error("Error validating funds for user {}: {}", user.getId(), e.getMessage());
            return false;
        }
        }
    
    /**
     * Deduct funds from source accounts
     */
    private void _deductFunds(User user, BigDecimal amount, String source) {
        try {
            // In a real implementation, you would deduct funds from the user's wallet and/or xysave account
            // For now, we'll just log the operation
            logger.info("Deducting {} from {} for user {}", amount, source, user.getId());
        } catch (Exception e) {
            logger.error("Error deducting funds for user {}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Error deducting funds", e);
        }
    }
    
    /**
     * Get fixed savings settings for a user
     */
    @Transactional(readOnly = true)
    public FixedSavingsSettingsDTO getFixedSavingsSettings(User user) {
        FixedSavingsSettings settings = fixedSavingsSettingsRepository.findByUser(user);
        return fixedSavingsMapper.toDTO(settings);
    }
    
    /**
     * Update fixed savings settings for a user
     */
    public FixedSavingsSettingsDTO updateFixedSavingsSettings(FixedSavingsSettingsDTO settingsDTO, User user) {
        FixedSavingsSettings settings = fixedSavingsSettingsRepository.findByUser(user);
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
            
            // Pay out to xysave account (placeholder)
            boolean success = true; // In a real implementation, this would depend on the actual payout operation
            
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
            newFixedSavings.setSource(FixedSavingsSource.XYSAVE.getCode()); // From xysave since that's where payout goes
            newFixedSavings.setPurpose(fixedSavings.getPurpose());
            newFixedSavings.setPurposeDescription("Auto-renewal of " + 
                (fixedSavings.getPurposeDescription() != null ? 
                    fixedSavings.getPurposeDescription() : 
                    fixedSavings.getPurpose()));
            newFixedSavings.setStartDate(newStartDate);
            newFixedSavings.setPaybackDate(newPaybackDate);
            newFixedSavings.setAutoRenewalEnabled(fixedSavings.getAutoRenewalEnabled());
            
            // Calculate interest rate
            newFixedSavings.setInterestRate(newFixedSavings.calculateInterestRate());
            
            // Calculate maturity amount
            newFixedSavings.setMaturityAmount(newFixedSavings.calculateMaturityAmount());
            
            // Generate account number
            newFixedSavings.setAccountNumber(generateAccountNumber());
            
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
            transaction.setCreatedAt(LocalDateTime.now());
            // Transaction would be saved in a real implementation
            
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
     * Generate a unique account number
     */
    private String generateAccountNumber() {
        return "FS" + System.currentTimeMillis() + (int)(Math.random() * 10000);
    }
}