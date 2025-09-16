package com.xypay.xypay.service;

import com.xypay.xypay.domain.XySaveAccount;
import com.xypay.xypay.domain.XySaveInvestment;
import com.xypay.xypay.domain.XySaveTransaction;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.XySaveAccountRepository;
import com.xypay.xypay.repository.XySaveInvestmentRepository;
import com.xypay.xypay.repository.XySaveTransactionRepository;
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
public class XySaveInvestmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(XySaveInvestmentService.class);
    
    @Autowired
    private XySaveInvestmentRepository xySaveInvestmentRepository;
    
    @Autowired
    private XySaveAccountRepository xySaveAccountRepository;
    
    @Autowired
    private XySaveTransactionRepository xySaveTransactionRepository;
    
    @Autowired
    private XySaveAccountService xySaveAccountService;
    
    /**
     * Create a new investment
     */
    @Transactional
    public XySaveInvestment createInvestment(User user, XySaveInvestment.InvestmentType investmentType, 
                                           BigDecimal amountInvested, BigDecimal expectedReturnRate, 
                                           LocalDate maturityDate) {
        try {
            XySaveAccount xySaveAccount = xySaveAccountService.getXySaveAccount(user);
            
            // Validate amount
            if (amountInvested.compareTo(xySaveAccount.getBalance()) > 0) {
                throw new IllegalArgumentException("Insufficient XySave balance for investment");
            }
            
            // Create investment
            XySaveInvestment investment = new XySaveInvestment();
            investment.setXysaveAccount(xySaveAccount);
            investment.setInvestmentType(investmentType);
            investment.setAmountInvested(amountInvested);
            investment.setCurrentValue(amountInvested); // Initially same as invested
            investment.setExpectedReturnRate(expectedReturnRate);
            investment.setMaturityDate(maturityDate);
            investment.setIsActive(true);
            
            XySaveInvestment savedInvestment = xySaveInvestmentRepository.save(investment);
            
            // Deduct from XySave balance
            xySaveAccount.setBalance(xySaveAccount.getBalance().subtract(amountInvested));
            xySaveAccountRepository.save(xySaveAccount);
            
            // Record transaction
            XySaveTransaction transaction = new XySaveTransaction();
            transaction.setXySaveAccount(xySaveAccount);
            transaction.setTransactionType(XySaveTransaction.TransactionType.TRANSFER_OUT);
            transaction.setAmount(amountInvested);
            transaction.setBalanceBefore(xySaveAccount.getBalance().add(amountInvested));
            transaction.setBalanceAfter(xySaveAccount.getBalance());
            transaction.setReference(generateReference("XS_INV"));
            transaction.setDescription("Investment in " + investmentType.getDescription());
            
            xySaveTransactionRepository.save(transaction);
            
            logger.info("Created {} investment for user {}", investmentType.getDescription(), user.getUsername());
            return savedInvestment;
            
        } catch (Exception e) {
            logger.error("Error creating investment for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Liquidate an investment
     */
    @Transactional
    public XySaveInvestment liquidateInvestment(User user, UUID investmentId) {
        try {
            XySaveInvestment investment = xySaveInvestmentRepository.findById(investmentId)
                .orElseThrow(() -> new RuntimeException("Investment not found"));
            
            // Verify ownership
            if (!investment.getXysaveAccount().getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Investment does not belong to user");
            }
            
            if (!Boolean.TRUE.equals(investment.getIsActive())) {
                throw new IllegalArgumentException("Investment is not active");
            }
            
            // Calculate return
            BigDecimal returnAmount = investment.getCurrentValue();
            
            // Update investment
            investment.setIsActive(false);
            xySaveInvestmentRepository.save(investment);
            
            // Add to XySave balance
            XySaveAccount xySaveAccount = investment.getXysaveAccount();
            xySaveAccount.setBalance(xySaveAccount.getBalance().add(returnAmount));
            xySaveAccountRepository.save(xySaveAccount);
            
            // Record transaction
            XySaveTransaction transaction = new XySaveTransaction();
            transaction.setXySaveAccount(xySaveAccount);
            transaction.setTransactionType(XySaveTransaction.TransactionType.TRANSFER_IN);
            transaction.setAmount(returnAmount);
            transaction.setBalanceBefore(xySaveAccount.getBalance().subtract(returnAmount));
            transaction.setBalanceAfter(xySaveAccount.getBalance());
            transaction.setReference(generateReference("XS_LIQ"));
            transaction.setDescription("Liquidated " + investment.getInvestmentType().getDescription() + " investment");
            
            xySaveTransactionRepository.save(transaction);
            
            logger.info("Liquidated {} investment for user {}", 
                investment.getInvestmentType().getDescription(), user.getUsername());
            return investment;
            
        } catch (Exception e) {
            logger.error("Error liquidating investment for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get investments for user
     */
    public List<XySaveInvestment> getUserInvestments(User user) {
        try {
            return xySaveInvestmentRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        } catch (Exception e) {
            logger.error("Error getting investments for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get active investments for user
     */
    public List<XySaveInvestment> getActiveInvestments(User user) {
        try {
            return xySaveInvestmentRepository.findActiveInvestmentsByUserId(user.getId());
        } catch (Exception e) {
            logger.error("Error getting active investments for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get investment by ID for user
     */
    public Optional<XySaveInvestment> getInvestmentById(User user, UUID investmentId) {
        try {
            return xySaveInvestmentRepository.findById(investmentId)
                .filter(investment -> investment.getXysaveAccount().getUser().getId().equals(user.getId()));
        } catch (Exception e) {
            logger.error("Error getting investment by ID for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Update investment value (for market fluctuations)
     */
    @Transactional
    public XySaveInvestment updateInvestmentValue(UUID investmentId, BigDecimal newValue) {
        try {
            XySaveInvestment investment = xySaveInvestmentRepository.findById(investmentId)
                .orElseThrow(() -> new RuntimeException("Investment not found"));
            
            investment.setCurrentValue(newValue);
            XySaveInvestment savedInvestment = xySaveInvestmentRepository.save(investment);
            
            logger.info("Updated investment value for {} to {}", 
                investment.getInvestmentType().getDescription(), newValue);
            return savedInvestment;
            
        } catch (Exception e) {
            logger.error("Error updating investment value: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get matured investments
     */
    public List<XySaveInvestment> getMaturedInvestments() {
        try {
            return xySaveInvestmentRepository.findMaturedInvestments(LocalDate.now());
        } catch (Exception e) {
            logger.error("Error getting matured investments: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get investment statistics for user
     */
    public InvestmentStatistics getInvestmentStatistics(User user) {
        try {
            List<XySaveInvestment> allInvestments = xySaveInvestmentRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
            List<XySaveInvestment> activeInvestments = xySaveInvestmentRepository.findActiveInvestmentsByUserId(user.getId());
            
            InvestmentStatistics stats = new InvestmentStatistics();
            stats.setTotalInvestments(allInvestments.size());
            stats.setActiveInvestments(activeInvestments.size());
            
            // Calculate totals
            BigDecimal totalInvested = allInvestments.stream()
                .map(XySaveInvestment::getAmountInvested)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setTotalInvested(totalInvested);
            
            BigDecimal totalCurrentValue = allInvestments.stream()
                .map(XySaveInvestment::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setTotalCurrentValue(totalCurrentValue);
            
            BigDecimal totalGainLoss = totalCurrentValue.subtract(totalInvested);
            stats.setTotalGainLoss(totalGainLoss);
            
            // Calculate overall return percentage
            if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal returnPercentage = totalGainLoss
                    .divide(totalInvested, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
                stats.setOverallReturnPercentage(returnPercentage);
            } else {
                stats.setOverallReturnPercentage(BigDecimal.ZERO);
            }
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Error getting investment statistics for user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Generate unique reference
     */
    private String generateReference(String prefix) {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return prefix + "_" + uuid;
    }
    
    /**
     * Investment statistics DTO
     */
    public static class InvestmentStatistics {
        private int totalInvestments;
        private int activeInvestments;
        private BigDecimal totalInvested;
        private BigDecimal totalCurrentValue;
        private BigDecimal totalGainLoss;
        private BigDecimal overallReturnPercentage;
        
        // Getters and setters
        public int getTotalInvestments() {
            return totalInvestments;
        }
        
        public void setTotalInvestments(int totalInvestments) {
            this.totalInvestments = totalInvestments;
        }
        
        public int getActiveInvestments() {
            return activeInvestments;
        }
        
        public void setActiveInvestments(int activeInvestments) {
            this.activeInvestments = activeInvestments;
        }
        
        public BigDecimal getTotalInvested() {
            return totalInvested;
        }
        
        public void setTotalInvested(BigDecimal totalInvested) {
            this.totalInvested = totalInvested;
        }
        
        public BigDecimal getTotalCurrentValue() {
            return totalCurrentValue;
        }
        
        public void setTotalCurrentValue(BigDecimal totalCurrentValue) {
            this.totalCurrentValue = totalCurrentValue;
        }
        
        public BigDecimal getTotalGainLoss() {
            return totalGainLoss;
        }
        
        public void setTotalGainLoss(BigDecimal totalGainLoss) {
            this.totalGainLoss = totalGainLoss;
        }
        
        public BigDecimal getOverallReturnPercentage() {
            return overallReturnPercentage;
        }
        
        public void setOverallReturnPercentage(BigDecimal overallReturnPercentage) {
            this.overallReturnPercentage = overallReturnPercentage;
        }
    }
}
