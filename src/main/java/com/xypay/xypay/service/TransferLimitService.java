package com.xypay.xypay.service;

import com.xypay.xypay.domain.*;
import com.xypay.xypay.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TransferLimitService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransferLimitService.class);
    
    @Autowired
    private TransferLimitRepository transferLimitRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Create a new transfer limit for a user
     */
    public TransferLimit createTransferLimit(User user, TransferLimit.LimitType limitType, 
                                           TransferLimit.LimitCategory limitCategory, 
                                           BigDecimal limitAmount, User createdBy) {
        try {
            // Check if limit already exists
            Optional<TransferLimit> existingLimit = transferLimitRepository
                    .findByUserAndLimitTypeAndLimitCategoryAndIsActiveTrue(
                        user, limitType, limitCategory);
            
            if (existingLimit.isPresent()) {
                throw new RuntimeException("Transfer limit already exists for this user and type");
            }
            
            TransferLimit transferLimit = new TransferLimit(
                user, limitType, limitCategory, limitAmount, LocalDateTime.now());
            transferLimit.setCreatedBy(createdBy);
            
            TransferLimit saved = transferLimitRepository.save(transferLimit);
            
            // Send notification
            notificationService.sendNotification(
                user.getId(),
                "TRANSFER_LIMIT_CREATED",
                String.format("New %s %s limit of %s has been set for your account", 
                    limitType.getValue(), limitCategory.getValue(), limitAmount)
            );
            
            logger.info("Created transfer limit for user {}: {} {} = {}", 
                user.getUsername(), limitType, limitCategory, limitAmount);
            
            return saved;
            
        } catch (Exception e) {
            logger.error("Error creating transfer limit: {}", e.getMessage());
            throw new RuntimeException("Failed to create transfer limit", e);
        }
    }
    
    /**
     * Check if user can perform transfer
     */
    public boolean canUserTransfer(User user, BigDecimal amount, 
                                  TransferLimit.LimitCategory category) {
        try {
            List<TransferLimit> applicableLimits = transferLimitRepository
                    .findByUserAndLimitCategoryAndIsActiveTrue(user, category);
            
            for (TransferLimit limit : applicableLimits) {
                if (!limit.canTransfer(amount)) {
                    logger.warn("Transfer limit exceeded for user {}: {} {} limit", 
                        user.getUsername(), limit.getLimitType(), limit.getLimitCategory());
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error checking transfer limits for user {}: {}", 
                user.getUsername(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Update transfer limit usage
     */
    public void updateTransferUsage(User user, BigDecimal amount, 
                                   TransferLimit.LimitCategory category) {
        try {
            List<TransferLimit> applicableLimits = transferLimitRepository
                    .findByUserAndLimitCategoryAndIsActiveTrue(user, category);
            
            for (TransferLimit limit : applicableLimits) {
                limit.useLimit(amount);
                transferLimitRepository.save(limit);
                
                // Check if notification should be sent
                if (limit.shouldSendNotification()) {
                    sendLimitNotification(limit);
                    limit.markNotificationSent();
                    transferLimitRepository.save(limit);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error updating transfer usage for user {}: {}", 
                user.getUsername(), e.getMessage());
        }
    }
    
    /**
     * Reset transfer limits (runs daily)
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight daily
    public void resetTransferLimits() {
        logger.info("Starting transfer limit reset process");
        
        try {
            List<TransferLimit> limitsToReset = transferLimitRepository
                    .findByAutoResetEnabledTrueAndIsActiveTrue();
            
            for (TransferLimit limit : limitsToReset) {
                if (limit.needsReset()) {
                    limit.resetLimit();
                    transferLimitRepository.save(limit);
                    
                    logger.info("Reset {} {} limit for user {}", 
                        limit.getLimitType(), limit.getLimitCategory(), 
                        limit.getUser().getUsername());
                }
            }
            
            logger.info("Completed transfer limit reset process");
            
        } catch (Exception e) {
            logger.error("Error resetting transfer limits: {}", e.getMessage());
        }
    }
    
    /**
     * Send limit notification
     */
    private void sendLimitNotification(TransferLimit limit) {
        try {
            String message = String.format(
                "You have used %.1f%% of your %s %s limit. Remaining: %s",
                limit.getUsagePercentage(),
                limit.getLimitType().getValue(),
                limit.getLimitCategory().getValue(),
                limit.getRemainingAmount()
            );
            
            notificationService.sendNotification(
                limit.getUser().getId(),
                "TRANSFER_LIMIT_WARNING",
                message
            );
            
        } catch (Exception e) {
            logger.error("Error sending limit notification: {}", e.getMessage());
        }
    }
    
    /**
     * Update transfer limit
     */
    public TransferLimit updateTransferLimit(UUID limitId, BigDecimal newLimitAmount, User updatedBy) {
        try {
            TransferLimit limit = transferLimitRepository.findById(limitId)
                    .orElseThrow(() -> new RuntimeException("Transfer limit not found"));
            
            BigDecimal oldAmount = limit.getLimitAmount();
            limit.setLimitAmount(newLimitAmount);
            limit.setRemainingAmount(newLimitAmount.subtract(limit.getUsedAmount()));
            
            TransferLimit updated = transferLimitRepository.save(limit);
            
            // Send notification
            notificationService.sendNotification(
                limit.getUser().getId(),
                "TRANSFER_LIMIT_UPDATED",
                String.format("Your %s %s limit has been updated from %s to %s", 
                    limit.getLimitType().getValue(), limit.getLimitCategory().getValue(),
                    oldAmount, newLimitAmount)
            );
            
            logger.info("Updated transfer limit {} for user {}: {} -> {}", 
                limitId, limit.getUser().getUsername(), oldAmount, newLimitAmount);
            
            return updated;
            
        } catch (Exception e) {
            logger.error("Error updating transfer limit {}: {}", limitId, e.getMessage());
            throw new RuntimeException("Failed to update transfer limit", e);
        }
    }
    
    /**
     * Deactivate transfer limit
     */
    public TransferLimit deactivateTransferLimit(UUID limitId, User deactivatedBy) {
        try {
            TransferLimit limit = transferLimitRepository.findById(limitId)
                    .orElseThrow(() -> new RuntimeException("Transfer limit not found"));
            
            limit.deactivate();
            TransferLimit updated = transferLimitRepository.save(limit);
            
            // Send notification
            notificationService.sendNotification(
                limit.getUser().getId(),
                "TRANSFER_LIMIT_DEACTIVATED",
                String.format("Your %s %s limit has been deactivated", 
                    limit.getLimitType().getValue(), limit.getLimitCategory().getValue())
            );
            
            logger.info("Deactivated transfer limit {} for user {}", 
                limitId, limit.getUser().getUsername());
            
            return updated;
            
        } catch (Exception e) {
            logger.error("Error deactivating transfer limit {}: {}", limitId, e.getMessage());
            throw new RuntimeException("Failed to deactivate transfer limit", e);
        }
    }
    
    /**
     * Get transfer limits by user
     */
    public List<TransferLimit> getTransferLimitsByUser(User user) {
        return transferLimitRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get active transfer limits by user
     */
    public List<TransferLimit> getActiveTransferLimitsByUser(User user) {
        return transferLimitRepository.findByUserAndIsActiveTrueOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get transfer limit by ID
     */
    public TransferLimit getTransferLimit(UUID id) {
        return transferLimitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer limit not found"));
    }
    
    /**
     * Get transfer limits by category
     */
    public List<TransferLimit> getTransferLimitsByCategory(TransferLimit.LimitCategory category) {
        return transferLimitRepository.findByLimitCategoryAndIsActiveTrue(category);
    }
    
    /**
     * Get transfer limits by type
     */
    public List<TransferLimit> getTransferLimitsByType(TransferLimit.LimitType limitType) {
        return transferLimitRepository.findByLimitTypeAndIsActiveTrue(limitType);
    }
    
    /**
     * Get transfer limit statistics
     */
    public TransferLimitStatistics getTransferLimitStatistics() {
        List<TransferLimit> allLimits = transferLimitRepository.findAll();
        
        TransferLimitStatistics stats = new TransferLimitStatistics();
        stats.setTotalLimits(allLimits.size());
        stats.setActiveLimits((int) allLimits.stream().filter(TransferLimit::getIsActive).count());
        stats.setInactiveLimits((int) allLimits.stream().filter(l -> !l.getIsActive()).count());
        
        // Calculate total limit amounts by category
        for (TransferLimit.LimitCategory category : TransferLimit.LimitCategory.values()) {
            BigDecimal totalLimit = allLimits.stream()
                    .filter(l -> l.getLimitCategory() == category && l.getIsActive())
                    .map(TransferLimit::getLimitAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.getTotalLimitsByCategory().put(category.getValue(), totalLimit);
        }
        
        return stats;
    }
    
    // Inner class for statistics
    public static class TransferLimitStatistics {
        private int totalLimits;
        private int activeLimits;
        private int inactiveLimits;
        private java.util.Map<String, BigDecimal> totalLimitsByCategory = new java.util.HashMap<>();
        
        // Getters and setters
        public int getTotalLimits() { return totalLimits; }
        public void setTotalLimits(int totalLimits) { this.totalLimits = totalLimits; }
        
        public int getActiveLimits() { return activeLimits; }
        public void setActiveLimits(int activeLimits) { this.activeLimits = activeLimits; }
        
        public int getInactiveLimits() { return inactiveLimits; }
        public void setInactiveLimits(int inactiveLimits) { this.inactiveLimits = inactiveLimits; }
        
        public java.util.Map<String, BigDecimal> getTotalLimitsByCategory() { return totalLimitsByCategory; }
        public void setTotalLimitsByCategory(java.util.Map<String, BigDecimal> totalLimitsByCategory) { 
            this.totalLimitsByCategory = totalLimitsByCategory; 
        }
    }
}
