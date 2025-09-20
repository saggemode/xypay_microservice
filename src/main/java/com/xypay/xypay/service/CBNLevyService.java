package com.xypay.xypay.service;

import com.xypay.xypay.domain.CBNLevy;
import com.xypay.xypay.enums.TransferType;
import com.xypay.xypay.repository.CBNLevyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CBNLevyService {
    
    private static final Logger logger = LoggerFactory.getLogger(CBNLevyService.class);
    
    @Autowired
    private CBNLevyRepository cbnLevyRepository;
    
    /**
     * Get all CBN levies with pagination
     */
    @Transactional(readOnly = true)
    public Page<CBNLevy> getAllLevies(Pageable pageable) {
        return cbnLevyRepository.findAll(pageable);
    }
    
    /**
     * Get all active CBN levies
     */
    @Transactional(readOnly = true)
    public List<CBNLevy> getActiveLevies() {
        return cbnLevyRepository.findByIsActiveTrue();
    }
    
    /**
     * Search CBN levies by name or description
     */
    @Transactional(readOnly = true)
    public Page<CBNLevy> searchLevies(String searchTerm, Pageable pageable) {
        return cbnLevyRepository.findByNameContainingIgnoreCase(searchTerm, pageable);
    }
    
    /**
     * Get CBN levy by ID
     */
    @Transactional(readOnly = true)
    public Optional<CBNLevy> getLevyById(UUID id) {
        return cbnLevyRepository.findById(id);
    }
    
    /**
     * Create a new CBN levy
     */
    public CBNLevy createLevy(CBNLevy levy) {
        try {
            levy.setCreatedAt(LocalDateTime.now());
            levy.setUpdatedAt(LocalDateTime.now());
            return cbnLevyRepository.save(levy);
        } catch (Exception e) {
            logger.error("Error creating CBN levy: {}", e.getMessage());
            throw new RuntimeException("Failed to create CBN levy", e);
        }
    }
    
    /**
     * Update an existing CBN levy
     */
    public CBNLevy updateLevy(UUID id, CBNLevy updatedLevy) {
        try {
            CBNLevy existingLevy = cbnLevyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CBN levy not found with id: " + id));
            
            // Update fields
            existingLevy.setName(updatedLevy.getName());
            existingLevy.setRate(updatedLevy.getRate());
            existingLevy.setFixedAmount(updatedLevy.getFixedAmount());
            existingLevy.setTransactionType(updatedLevy.getTransactionType());
            existingLevy.setMinAmount(updatedLevy.getMinAmount());
            existingLevy.setMaxAmount(updatedLevy.getMaxAmount());
            existingLevy.setIsActive(updatedLevy.getIsActive());
            existingLevy.setEffectiveFrom(updatedLevy.getEffectiveFrom());
            existingLevy.setEffectiveTo(updatedLevy.getEffectiveTo());
            existingLevy.setRegulationReference(updatedLevy.getRegulationReference());
            existingLevy.setDescription(updatedLevy.getDescription());
            existingLevy.setUpdatedAt(LocalDateTime.now());
            
            return cbnLevyRepository.save(existingLevy);
        } catch (Exception e) {
            logger.error("Error updating CBN levy {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update CBN levy", e);
        }
    }
    
    /**
     * Delete a CBN levy
     */
    public boolean deleteLevy(UUID id) {
        try {
            if (cbnLevyRepository.existsById(id)) {
                cbnLevyRepository.deleteById(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting CBN levy {}: {}", id, e.getMessage());
            return false;
        }
    }
    
    /**
     * Toggle levy active status
     */
    public CBNLevy toggleLevyStatus(UUID id) {
        try {
            CBNLevy levy = cbnLevyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CBN levy not found with id: " + id));
            
            levy.setIsActive(!levy.getIsActive());
            levy.setUpdatedAt(LocalDateTime.now());
            
            return cbnLevyRepository.save(levy);
        } catch (Exception e) {
            logger.error("Error toggling CBN levy status {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to toggle CBN levy status", e);
        }
    }
    
    /**
     * Get levies by transaction type
     */
    @Transactional(readOnly = true)
    public List<CBNLevy> getLeviesByTransactionType(String transactionType) {
        return cbnLevyRepository.findByTransactionTypeAndIsActiveTrue(transactionType);
    }
    
    /**
     * Get effective levies for a specific date
     */
    @Transactional(readOnly = true)
    public List<CBNLevy> getEffectiveLevies(LocalDateTime date) {
        return cbnLevyRepository.findEffectiveLevies(date);
    }
    
    /**
     * Get applicable levies for a transaction amount
     */
    @Transactional(readOnly = true)
    public List<CBNLevy> getApplicableLevies(BigDecimal amount) {
        return cbnLevyRepository.findApplicableLevies(amount, amount);
    }
    
    
    /**
     * Get levy statistics
     */
    @Transactional(readOnly = true)
    public LevyStatistics getLevyStatistics() {
        long totalLevies = cbnLevyRepository.count();
        long activeLevies = cbnLevyRepository.countByIsActiveTrue();
        long inactiveLevies = totalLevies - activeLevies;
        
        List<CBNLevy> expiringSoon = cbnLevyRepository.findExpiringSoon(LocalDateTime.now().plusDays(30));
        
        return new LevyStatistics(totalLevies, activeLevies, inactiveLevies, expiringSoon.size());
    }
    
    /**
     * Create default CBN levies if none exist
     */
    public void createDefaultLevies() {
        if (cbnLevyRepository.count() == 0) {
            logger.info("Creating default CBN levies");
            
            // Default CBN levy for all transactions
            CBNLevy defaultLevy = new CBNLevy();
            defaultLevy.setName("CBN Electronic Transfer Levy");
            defaultLevy.setFixedAmount(new BigDecimal("50.00"));
            defaultLevy.setTransactionType(TransferType.INTERNAL);
            defaultLevy.setMinAmount(new BigDecimal("10000.00"));
            defaultLevy.setIsActive(true);
            defaultLevy.setEffectiveFrom(LocalDateTime.now());
            defaultLevy.setRegulationReference("CBN Circular 2023");
            defaultLevy.setDescription("Central Bank of Nigeria electronic transfer levy as per CBN regulations");
            createLevy(defaultLevy);
            
            // External transfer levy
            CBNLevy externalLevy = new CBNLevy();
            externalLevy.setName("External Transfer Levy");
            externalLevy.setFixedAmount(new BigDecimal("100.00"));
            externalLevy.setTransactionType(TransferType.EXTERNAL);
            externalLevy.setMinAmount(new BigDecimal("5000.00"));
            externalLevy.setIsActive(true);
            externalLevy.setEffectiveFrom(LocalDateTime.now());
            externalLevy.setRegulationReference("CBN Circular 2023");
            externalLevy.setDescription("Additional levy for external money transfers");
            createLevy(externalLevy);
        }
    }
    
    /**
     * Inner class for levy statistics
     */
    public static class LevyStatistics {
        private final long totalLevies;
        private final long activeLevies;
        private final long inactiveLevies;
        private final long expiringSoon;
        
        public LevyStatistics(long totalLevies, long activeLevies, long inactiveLevies, long expiringSoon) {
            this.totalLevies = totalLevies;
            this.activeLevies = activeLevies;
            this.inactiveLevies = inactiveLevies;
            this.expiringSoon = expiringSoon;
        }
        
        // Getters
        public long getTotalLevies() { return totalLevies; }
        public long getActiveLevies() { return activeLevies; }
        public long getInactiveLevies() { return inactiveLevies; }
        public long getExpiringSoon() { return expiringSoon; }
    }
}
