package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class TargetSavingService {
    
    @Autowired
    private TargetSavingRepository targetSavingRepository;
    
    @Autowired
    private TargetSavingDepositRepository targetSavingDepositRepository;
    
    @Autowired
    private TargetSavingWithdrawalRepository targetSavingWithdrawalRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private XySaveAccountRepository xySaveAccountRepository;
    
    @Autowired
    private TargetSavingNotificationService notificationService;
    
    private boolean validateSufficientFunds(User user, BigDecimal amount, TargetSavingSource source) {
        try {
            List<Wallet> wallets = walletRepository.findByUser(user);
            if (wallets.isEmpty()) {
                return false;
            }
            Wallet wallet = wallets.get(0);
            
            if (source == TargetSavingSource.WALLET) {
                return wallet.getBalance().compareTo(amount) >= 0;
            } else if (source == TargetSavingSource.XYSAVE) {
                Optional<XySaveAccount> xySaveOpt = xySaveAccountRepository.findByUser(user);
                if (xySaveOpt.isEmpty()) {
                    return false;
                }
                return xySaveOpt.get().getBalance().compareTo(amount) >= 0;
            } else if (source == TargetSavingSource.BOTH) {
                Optional<XySaveAccount> xySaveOpt = xySaveAccountRepository.findByUser(user);
                if (xySaveOpt.isEmpty()) {
                    return false;
                }
                BigDecimal halfAmount = amount.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
                return wallet.getBalance().compareTo(halfAmount) >= 0 && 
                       xySaveOpt.get().getBalance().compareTo(halfAmount) >= 0;
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error validating sufficient funds: {}", e.getMessage());
            return false;
        }
    }
    
    @Transactional
    public Map<String, Object> createTargetSaving(User user, Map<String, Object> data) {
        try {
            // Validate required fields
            String[] requiredFields = {"name", "category", "target_amount", "frequency", "start_date", "end_date"};
            for (String field : requiredFields) {
                if (!data.containsKey(field)) {
                    return Map.of("success", false, "message", "Missing required field: " + field);
                }
            }
            
            // Validate frequency and preferred deposit day
            TargetSavingFrequency frequency = (TargetSavingFrequency) data.get("frequency");
            String preferredDepositDay = (String) data.get("preferred_deposit_day");
            
            if ((frequency == TargetSavingFrequency.WEEKLY || 
                 frequency == TargetSavingFrequency.MONTHLY) && 
                (preferredDepositDay == null || preferredDepositDay.trim().isEmpty())) {
                return Map.of("success", false, "message", "Preferred deposit day is required for weekly/monthly frequency");
            }
            
            // Get optional enhanced fields
            TargetSavingSource source = (TargetSavingSource) data.getOrDefault("source", TargetSavingSource.WALLET);
            Boolean strictMode = (Boolean) data.getOrDefault("strict_mode", false);
            BigDecimal targetAmount = (BigDecimal) data.get("target_amount");
            
            // Validate that the user has sufficient funds in the selected source
            if (!validateSufficientFunds(user, targetAmount, source)) {
                return Map.of("success", false, "message", "Insufficient funds in " + source.getCode() + " for target saving");
            }
            
            // Generate a unique account number
            String accountNumber = generateAccountNumber(user);
            
            // Create target saving
            TargetSaving targetSaving = new TargetSaving();
            targetSaving.setUser(user);
            targetSaving.setName((String) data.get("name"));
            targetSaving.setCategory((TargetSavingCategory) data.get("category"));
            targetSaving.setTargetAmount(targetAmount);
            targetSaving.setFrequency(frequency);
            targetSaving.setPreferredDepositDay(preferredDepositDay);
            targetSaving.setStartDate((LocalDate) data.get("start_date"));
            targetSaving.setEndDate((LocalDate) data.get("end_date"));
            targetSaving.setAccountNumber(accountNumber);
            targetSaving.setSource(source);
            targetSaving.setStrictMode(strictMode);
            
            targetSaving = targetSavingRepository.save(targetSaving);
            
            // Send creation notification
            notificationService.sendTargetCreatedNotification(user, targetSaving);
            
            log.info("Target saving created for user {}: {}", user.getUsername(), targetSaving.getName());
            
            return Map.of(
                "success", true,
                "target_saving", targetSaving,
                "message", "Target saving created successfully"
            );
            
        } catch (Exception e) {
            log.error("Error creating target saving: {}", e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    private String generateAccountNumber(User user) {
        Random random = new Random();
        String accountNumber;
        do {
            accountNumber = "TS" + String.format("%04d", user.getId().hashCode() % 10000) + random.nextInt(100000, 999999);
        } while (targetSavingRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
    
    @Transactional
    public Map<String, Object> updateTargetSaving(User user, UUID targetId, Map<String, Object> data) {
        try {
            Optional<TargetSaving> targetOpt = targetSavingRepository.findByIdAndUser(targetId, user);
            if (targetOpt.isEmpty()) {
                return Map.of("success", false, "message", "Target saving not found");
            }
            
            TargetSaving targetSaving = targetOpt.get();
            
            // Update fields
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();
                
                switch (field) {
                    case "name":
                        targetSaving.setName((String) value);
                        break;
                    case "category":
                        targetSaving.setCategory((TargetSavingCategory) value);
                        break;
                    case "target_amount":
                        targetSaving.setTargetAmount((BigDecimal) value);
                        break;
                    case "frequency":
                        targetSaving.setFrequency((TargetSavingFrequency) value);
                        break;
                    case "preferred_deposit_day":
                        targetSaving.setPreferredDepositDay((String) value);
                        break;
                    case "start_date":
                        targetSaving.setStartDate((LocalDate) value);
                        break;
                    case "end_date":
                        targetSaving.setEndDate((LocalDate) value);
                        break;
                    case "source":
                        targetSaving.setSource((TargetSavingSource) value);
                        break;
                    case "strict_mode":
                        targetSaving.setStrictMode((Boolean) value);
                        break;
                }
            }
            
            targetSaving = targetSavingRepository.save(targetSaving);
            
            // Send update notification
            notificationService.sendTargetUpdatedNotification(user, targetSaving);
            
            log.info("Target saving updated for user {}: {}", user.getUsername(), targetSaving.getName());
            
            return Map.of(
                "success", true,
                "target_saving", targetSaving,
                "message", "Target saving updated successfully"
            );
            
        } catch (Exception e) {
            log.error("Error updating target saving: {}", e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    @Transactional
    public Map<String, Object> makeDeposit(User user, UUID targetId, BigDecimal amount, String notes) {
        try {
            Optional<TargetSaving> targetOpt = targetSavingRepository.findByIdAndUser(targetId, user);
            if (targetOpt.isEmpty()) {
                return Map.of("success", false, "message", "Target saving not found");
            }
            
            TargetSaving targetSaving = targetOpt.get();
            
            if (!targetSaving.getIsActive()) {
                return Map.of("success", false, "message", "Target saving is not active");
            }
            
            if (targetSaving.getIsCompleted()) {
                return Map.of("success", false, "message", "Target saving is already completed");
            }
            
            // Validate that the user has sufficient funds in the selected source
            if (!validateSufficientFunds(user, amount, targetSaving.getSource())) {
                return Map.of("success", false, "message", "Insufficient funds in " + targetSaving.getSource().getCode() + " for deposit");
            }
            
            // Process the withdrawal from appropriate source(s)
            processWithdrawalFromSource(user, amount, targetSaving.getSource());
            
            // Create deposit
            TargetSavingDeposit deposit = new TargetSavingDeposit();
            deposit.setTargetSaving(targetSaving);
            deposit.setAmount(amount);
            deposit.setNotes(notes);
            deposit.setSource(targetSaving.getSource());
            deposit.setDepositDate(LocalDateTime.now());
            
            deposit = targetSavingDepositRepository.save(deposit);
            
            // Update target saving
            targetSaving.setCurrentAmount(targetSaving.getCurrentAmount().add(amount));
            targetSaving = targetSavingRepository.save(targetSaving);
            
            // Send deposit notification
            notificationService.sendDepositNotification(user, targetSaving, deposit);
            
            // Check for milestones
            notificationService.checkAndSendMilestoneNotifications(user, targetSaving);
            
            // Check if target is completed
            if (targetSaving.getIsCompleted()) {
                notificationService.sendTargetCompletedNotification(user, targetSaving);
            }
            
            log.info("Deposit made to target saving {}: {}", targetSaving.getName(), amount);
            
            return Map.of(
                "success", true,
                "deposit", deposit,
                "target_saving", targetSaving,
                "message", "Deposit made successfully"
            );
            
        } catch (Exception e) {
            log.error("Error making deposit: {}", e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    private void processWithdrawalFromSource(User user, BigDecimal amount, TargetSavingSource source) {
        if (source == TargetSavingSource.WALLET) {
            List<Wallet> wallets = walletRepository.findByUser(user);
            if (wallets.isEmpty()) {
                throw new RuntimeException("Wallet not found");
            }
            Wallet wallet = wallets.get(0);
            wallet.setBalance(wallet.getBalance().subtract(amount));
            walletRepository.save(wallet);
            
        } else if (source == TargetSavingSource.XYSAVE) {
            XySaveAccount xySaveAccount = xySaveAccountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("XySave account not found"));
            xySaveAccount.setBalance(xySaveAccount.getBalance().subtract(amount));
            xySaveAccountRepository.save(xySaveAccount);
            
        } else if (source == TargetSavingSource.BOTH) {
            BigDecimal halfAmount = amount.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            
            List<Wallet> wallets = walletRepository.findByUser(user);
            if (wallets.isEmpty()) {
                throw new RuntimeException("Wallet not found");
            }
            Wallet wallet = wallets.get(0);
            wallet.setBalance(wallet.getBalance().subtract(halfAmount));
            walletRepository.save(wallet);
            
            XySaveAccount xySaveAccount = xySaveAccountRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("XySave account not found"));
            xySaveAccount.setBalance(xySaveAccount.getBalance().subtract(halfAmount));
            xySaveAccountRepository.save(xySaveAccount);
        }
    }
    
    public Map<String, Object> getUserTargets(User user, boolean activeOnly) {
        try {
            List<TargetSaving> targets;
            if (activeOnly) {
                targets = targetSavingRepository.findByUserAndIsActiveTrue(user);
            } else {
                targets = targetSavingRepository.findByUser(user);
            }
            
            return Map.of(
                "success", true,
                "targets", targets,
                "count", targets.size()
            );
        } catch (Exception e) {
            log.error("Error getting user targets: {}", e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    public Map<String, Object> getTargetDetails(User user, UUID targetId) {
        try {
            Optional<TargetSaving> targetOpt = targetSavingRepository.findByIdAndUser(targetId, user);
            if (targetOpt.isEmpty()) {
                return Map.of("success", false, "message", "Target saving not found");
            }
            
            TargetSaving targetSaving = targetOpt.get();
            List<TargetSavingDeposit> recentDeposits = targetSavingDepositRepository
                .findByTargetSavingOrderByDepositDateDesc(targetSaving, 10);
            
            return Map.of(
                "success", true,
                "target_saving", targetSaving,
                "recent_deposits", recentDeposits,
                "total_deposits", targetSaving.getDeposits().size(),
                "progress_percentage", targetSaving.getProgressPercentage(),
                "remaining_amount", targetSaving.getRemainingAmount(),
                "days_remaining", targetSaving.getDaysRemaining()
            );
        } catch (Exception e) {
            log.error("Error getting target details: {}", e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    @Transactional
    public Map<String, Object> deactivateTarget(User user, UUID targetId) {
        try {
            Optional<TargetSaving> targetOpt = targetSavingRepository.findByIdAndUser(targetId, user);
            if (targetOpt.isEmpty()) {
                return Map.of("success", false, "message", "Target saving not found");
            }
            
            TargetSaving targetSaving = targetOpt.get();
            targetSaving.setIsActive(false);
            targetSaving = targetSavingRepository.save(targetSaving);
            
            // Send deactivation notification
            notificationService.sendTargetDeactivatedNotification(user, targetSaving);
            
            log.info("Target saving deactivated for user {}: {}", user.getUsername(), targetSaving.getName());
            
            return Map.of("success", true, "message", "Target saving deactivated successfully");
            
        } catch (Exception e) {
            log.error("Error deactivating target saving: {}", e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    public Map<String, Object> getTargetAnalytics(User user, UUID targetId) {
        try {
            Optional<TargetSaving> targetOpt = targetSavingRepository.findByIdAndUser(targetId, user);
            if (targetOpt.isEmpty()) {
                return Map.of("success", false, "message", "Target saving not found");
            }
            
            TargetSaving targetSaving = targetOpt.get();
            List<TargetSavingDeposit> deposits = targetSaving.getDeposits();
            
            // Calculate analytics
            int totalDeposits = deposits.size();
            BigDecimal averageDeposit = BigDecimal.ZERO;
            if (totalDeposits > 0) {
                BigDecimal totalAmount = deposits.stream()
                    .map(TargetSavingDeposit::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                averageDeposit = totalAmount.divide(new BigDecimal(totalDeposits), 2, RoundingMode.HALF_UP);
            }
            
            // Get deposit frequency
            double depositFrequency = 0;
            if (totalDeposits > 0) {
                long daysSinceStart = ChronoUnit.DAYS.between(targetSaving.getStartDate(), LocalDate.now());
                depositFrequency = (double) daysSinceStart / totalDeposits;
            }
            
            Map<String, Object> analytics = Map.of(
                "total_deposits", totalDeposits,
                "average_deposit", averageDeposit,
                "deposit_frequency", depositFrequency,
                "progress_percentage", targetSaving.getProgressPercentage(),
                "remaining_amount", targetSaving.getRemainingAmount(),
                "days_remaining", targetSaving.getDaysRemaining(),
                "is_overdue", targetSaving.isOverdue(),
                "daily_target", targetSaving.getDailyTarget(),
                "weekly_target", targetSaving.getWeeklyTarget(),
                "monthly_target", targetSaving.getMonthlyTarget()
            );
            
            return Map.of("success", true, "analytics", analytics);
            
        } catch (Exception e) {
            log.error("Error getting target analytics: {}", e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    @Transactional
    public Map<String, Object> withdrawFromTarget(User user, UUID targetId, BigDecimal amount, String destination) {
        try {
            Optional<TargetSaving> targetOpt = targetSavingRepository.findByIdAndUser(targetId, user);
            if (targetOpt.isEmpty()) {
                return Map.of("success", false, "message", "Target saving not found");
            }
            
            TargetSaving targetSaving = targetOpt.get();
            
            // Validate target saving is active
            if (!targetSaving.getIsActive()) {
                return Map.of("success", false, "message", "Cannot withdraw from an inactive target saving");
            }
            
            // Validate amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return Map.of("success", false, "message", "Withdrawal amount must be greater than zero");
            }
            
            // Validate sufficient funds
            if (amount.compareTo(targetSaving.getCurrentAmount()) > 0) {
                return Map.of("success", false, "message", "Insufficient funds in target saving");
            }
            
            // Process withdrawal
            targetSaving.setCurrentAmount(targetSaving.getCurrentAmount().subtract(amount));
            targetSaving = targetSavingRepository.save(targetSaving);
            
            // Credit user's wallet or XySave based on destination
            String destinationAccount;
            if ("wallet".equals(destination)) {
                List<Wallet> wallets = walletRepository.findByUser(user);
                if (wallets.isEmpty()) {
                    throw new RuntimeException("Wallet not found");
                }
                Wallet wallet = wallets.get(0);
                wallet.setBalance(wallet.getBalance().add(amount));
                walletRepository.save(wallet);
                destinationAccount = "wallet";
                
            } else if ("xysave".equals(destination)) {
                XySaveAccount xySaveAccount = xySaveAccountRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("XySave account not found"));
                xySaveAccount.setBalance(xySaveAccount.getBalance().add(amount));
                xySaveAccountRepository.save(xySaveAccount);
                destinationAccount = "xysave";
                
            } else {
                return Map.of("success", false, "message", "Invalid destination account");
            }
            
            // Create withdrawal record
            TargetSavingWithdrawal withdrawal = new TargetSavingWithdrawal();
            withdrawal.setTargetSaving(targetSaving);
            withdrawal.setAmount(amount);
            withdrawal.setWithdrawalDate(LocalDateTime.now());
            withdrawal.setDestination(TargetSavingSource.valueOf(destinationAccount.toUpperCase()));
            withdrawal.setNotes("Withdrawal to " + destinationAccount);
            
            withdrawal = targetSavingWithdrawalRepository.save(withdrawal);
            
            // Send withdrawal notification
            notificationService.sendWithdrawalNotification(user, targetSaving, withdrawal);
            
            log.info("Withdrawal of {} from target saving {} for user {}", amount, targetSaving.getName(), user.getUsername());
            
            return Map.of(
                "success", true,
                "message", "Successfully withdrew " + amount + " from target saving",
                "withdrawal", withdrawal
            );
            
        } catch (Exception e) {
            log.error("Error withdrawing from target saving: {}", e.getMessage());
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}
