package com.xypay.xypay.service;

import com.xypay.xypay.domain.XySaveGoal;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.XySaveGoalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class XySaveGoalService {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveGoalService.class);
    
    @Autowired
    private XySaveGoalRepository xySaveGoalRepository;
    
    /**
     * Create a new savings goal
     */
    @Transactional
    public XySaveGoal createGoal(User user, String name, BigDecimal targetAmount, LocalDate targetDate) {
        try {
            XySaveGoal goal = new XySaveGoal();
            goal.setUser(user);
            goal.setName(name);
            goal.setTargetAmount(targetAmount);
            goal.setTargetDate(targetDate);
            goal.setCurrentAmount(BigDecimal.ZERO);
            goal.setIsActive(true);
            
            XySaveGoal savedGoal = xySaveGoalRepository.save(goal);
            
            logger.info("Created goal '{}' for user {}", name, user.getUsername());
            return savedGoal;
            
        } catch (Exception e) {
            logger.error("Error creating goal for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Update goal progress with amount
     */
    @Transactional
    public XySaveGoal updateGoalProgress(User user, UUID goalId, BigDecimal amount) {
        try {
            XySaveGoal goal = xySaveGoalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
            
            goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
            xySaveGoalRepository.save(goal);
            
            logger.info("Updated goal '{}' progress for user {}", goal.getName(), user.getUsername());
            return goal;
            
        } catch (Exception e) {
            logger.error("Error updating goal progress for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get all goals for user
     */
    public List<XySaveGoal> getUserGoals(User user) {
        try {
            return xySaveGoalRepository.findByUserAndIsActiveTrueOrderByCreatedAtDesc(user);
        } catch (Exception e) {
            logger.error("Error getting goals for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get active goals for user
     */
    public List<XySaveGoal> getActiveGoals(User user) {
        try {
            return xySaveGoalRepository.findByUserAndIsActiveTrue(user);
        } catch (Exception e) {
            logger.error("Error getting active goals for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get completed goals for user
     */
    public List<XySaveGoal> getCompletedGoals(User user) {
        try {
            return xySaveGoalRepository.findCompletedGoalsByUser(user);
        } catch (Exception e) {
            logger.error("Error getting completed goals for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get goal by ID for user
     */
    public Optional<XySaveGoal> getGoalById(User user, UUID goalId) {
        try {
            return xySaveGoalRepository.findByIdAndUser(goalId, user);
        } catch (Exception e) {
            logger.error("Error getting goal by ID for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Update goal
     */
    @Transactional
    public XySaveGoal updateGoal(User user, UUID goalId, String name, BigDecimal targetAmount, LocalDate targetDate) {
        try {
            XySaveGoal goal = xySaveGoalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
            
            if (name != null) {
                goal.setName(name);
            }
            if (targetAmount != null) {
                goal.setTargetAmount(targetAmount);
            }
            if (targetDate != null) {
                goal.setTargetDate(targetDate);
            }
            
            XySaveGoal savedGoal = xySaveGoalRepository.save(goal);
            
            logger.info("Updated goal '{}' for user {}", savedGoal.getName(), user.getUsername());
            return savedGoal;
            
        } catch (Exception e) {
            logger.error("Error updating goal for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Deactivate goal
     */
    @Transactional
    public XySaveGoal deactivateGoal(User user, UUID goalId) {
        try {
            XySaveGoal goal = xySaveGoalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
            
            goal.setIsActive(false);
            XySaveGoal savedGoal = xySaveGoalRepository.save(goal);
            
            logger.info("Deactivated goal '{}' for user {}", savedGoal.getName(), user.getUsername());
            return savedGoal;
            
        } catch (Exception e) {
            logger.error("Error deactivating goal for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Delete goal
     */
    @Transactional
    public void deleteGoal(User user, UUID goalId) {
        try {
            XySaveGoal goal = xySaveGoalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
            
            xySaveGoalRepository.delete(goal);
            
            logger.info("Deleted goal '{}' for user {}", goal.getName(), user.getUsername());
            
        } catch (Exception e) {
            logger.error("Error deleting goal for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get goal statistics for user
     */
    public GoalStatistics getGoalStatistics(User user) {
        try {
            List<XySaveGoal> allGoals = xySaveGoalRepository.findByUser(user);
            List<XySaveGoal> activeGoals = xySaveGoalRepository.findByUserAndIsActiveTrue(user);
            List<XySaveGoal> completedGoals = xySaveGoalRepository.findCompletedGoalsByUser(user);
            
            GoalStatistics stats = new GoalStatistics();
            stats.setTotalGoals(allGoals.size());
            stats.setActiveGoals(activeGoals.size());
            stats.setCompletedGoals(completedGoals.size());
            
            // Calculate total target amount
            BigDecimal totalTargetAmount = activeGoals.stream()
                .map(XySaveGoal::getTargetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setTotalTargetAmount(totalTargetAmount);
            
            // Calculate total current amount
            BigDecimal totalCurrentAmount = activeGoals.stream()
                .map(XySaveGoal::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setTotalCurrentAmount(totalCurrentAmount);
            
            // Calculate overall progress percentage
            if (totalTargetAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal progressPercentage = totalCurrentAmount
                    .divide(totalTargetAmount, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
                stats.setOverallProgressPercentage(progressPercentage);
            } else {
                stats.setOverallProgressPercentage(BigDecimal.ZERO);
            }
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Error getting goal statistics for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Goal statistics DTO
     */
    public static class GoalStatistics {
        private int totalGoals;
        private int activeGoals;
        private int completedGoals;
        private BigDecimal totalTargetAmount;
        private BigDecimal totalCurrentAmount;
        private BigDecimal overallProgressPercentage;
        
        // Getters and setters
        public int getTotalGoals() {
            return totalGoals;
        }
        
        public void setTotalGoals(int totalGoals) {
            this.totalGoals = totalGoals;
        }
        
        public int getActiveGoals() {
            return activeGoals;
        }
        
        public void setActiveGoals(int activeGoals) {
            this.activeGoals = activeGoals;
        }
        
        public int getCompletedGoals() {
            return completedGoals;
        }
        
        public void setCompletedGoals(int completedGoals) {
            this.completedGoals = completedGoals;
        }
        
        public BigDecimal getTotalTargetAmount() {
            return totalTargetAmount;
        }
        
        public void setTotalTargetAmount(BigDecimal totalTargetAmount) {
            this.totalTargetAmount = totalTargetAmount;
        }
        
        public BigDecimal getTotalCurrentAmount() {
            return totalCurrentAmount;
        }
        
        public void setTotalCurrentAmount(BigDecimal totalCurrentAmount) {
            this.totalCurrentAmount = totalCurrentAmount;
        }
        
        public BigDecimal getOverallProgressPercentage() {
            return overallProgressPercentage;
        }
        
        public void setOverallProgressPercentage(BigDecimal overallProgressPercentage) {
            this.overallProgressPercentage = overallProgressPercentage;
        }
    }
}
