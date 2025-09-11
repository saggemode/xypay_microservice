package com.xypay.xypay.service;

import com.xypay.xypay.domain.InterestRule;
import com.xypay.xypay.domain.InterestTier;
import com.xypay.xypay.domain.Bank;
import com.xypay.xypay.repository.InterestRuleRepository;
import com.xypay.xypay.repository.BankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InterestRuleService {
    
    @Autowired
    private InterestRuleRepository interestRuleRepository;
    
    @Autowired
    private BankRepository bankRepository;
    
    public InterestRule createInterestRule(Long bankId, String ruleCode, String ruleName, 
                                         InterestRule.RuleType ruleType, 
                                         InterestRule.ProductCategory productCategory,
                                         BigDecimal baseRate, String currencyCode) {
        Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new RuntimeException("Bank not found"));
        
        InterestRule rule = new InterestRule();
        rule.setBank(bank);
        rule.setRuleCode(ruleCode);
        rule.setRuleName(ruleName);
        rule.setRuleType(ruleType);
        rule.setProductCategory(productCategory);
        rule.setBaseRate(baseRate);
        rule.setCurrencyCode(currencyCode);
        rule.setEffectiveDate(LocalDateTime.now());
        rule.setIsActive(true);
        rule.setApprovalStatus(InterestRule.ApprovalStatus.PENDING);
        
        return interestRuleRepository.save(rule);
    }
    
    public List<InterestRule> getActiveRulesByBank(Long bankId) {
        return interestRuleRepository.findActiveRulesByBank(bankId, LocalDateTime.now());
    }
    
    public List<InterestRule> getRulesByTypeAndCategory(Long bankId, 
                                                       InterestRule.RuleType ruleType,
                                                       InterestRule.ProductCategory productCategory) {
        return interestRuleRepository.findActiveRulesByTypeAndCategory(
            bankId, ruleType, productCategory, LocalDateTime.now());
    }
    
    public List<InterestRule> findApplicableRules(Long bankId, 
                                                 InterestRule.RuleType ruleType,
                                                 String currencyCode, 
                                                 BigDecimal amount) {
        return interestRuleRepository.findApplicableRules(
            bankId, ruleType, currencyCode, amount, LocalDateTime.now());
    }
    
    public Optional<InterestRule> getBestRateRule(Long bankId, 
                                                 InterestRule.RuleType ruleType,
                                                 String currencyCode, 
                                                 BigDecimal amount,
                                                 String customerCategory) {
        List<InterestRule> applicableRules = findApplicableRules(bankId, ruleType, currencyCode, amount);
        
        // Filter by customer category if provided
        if (customerCategory != null) {
            applicableRules = applicableRules.stream()
                .filter(rule -> customerCategory.equals(rule.getCustomerCategory()) || 
                               rule.getCustomerCategory() == null)
                .toList();
        }
        
        // Find rule with highest effective rate for deposits, lowest for loans
        return applicableRules.stream()
            .max((rule1, rule2) -> {
                BigDecimal rate1 = calculateEffectiveRate(rule1, amount);
                BigDecimal rate2 = calculateEffectiveRate(rule2, amount);
                
                // For loan interest, prefer lower rates; for deposit interest, prefer higher rates
                if (ruleType == InterestRule.RuleType.LOAN_INTEREST || 
                    ruleType == InterestRule.RuleType.OVERDRAFT_INTEREST) {
                    return rate2.compareTo(rate1); // Lower is better for loans
                } else {
                    return rate1.compareTo(rate2); // Higher is better for deposits
                }
            });
    }
    
    public BigDecimal calculateInterest(Long ruleId, BigDecimal principal, Integer days) {
        InterestRule rule = interestRuleRepository.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("Interest rule not found"));
        
        return calculateInterestWithRule(rule, principal, days);
    }
    
    public BigDecimal calculateInterestWithRule(InterestRule rule, BigDecimal principal, Integer days) {
        if (!rule.isCurrentlyActive()) {
            throw new IllegalStateException("Interest rule is not active: " + rule.getRuleCode());
        }
        
        if (rule.getIsTiered() && rule.getInterestTiers() != null && !rule.getInterestTiers().isEmpty()) {
            return calculateTieredInterest(rule, principal, days);
        } else {
            return rule.calculateInterest(principal, days);
        }
    }
    
    private BigDecimal calculateTieredInterest(InterestRule rule, BigDecimal principal, Integer days) {
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal remainingAmount = principal;
        
        List<InterestTier> tiers = rule.getInterestTiers().stream()
            .filter(InterestTier::getIsActive)
            .sorted((t1, t2) -> t1.getTierSequence().compareTo(t2.getTierSequence()))
            .toList();
        
        for (InterestTier tier : tiers) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            
            BigDecimal tierAmount = tier.getTierAmount(principal);
            if (tierAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal tierInterest = calculateTierInterest(tier, tierAmount, days, rule.getCalculationMethod());
                totalInterest = totalInterest.add(tierInterest);
                remainingAmount = remainingAmount.subtract(tierAmount);
            }
        }
        
        return totalInterest;
    }
    
    private BigDecimal calculateTierInterest(InterestTier tier, BigDecimal amount, Integer days, 
                                           InterestRule.CalculationMethod calculationMethod) {
        BigDecimal dailyRate = tier.getInterestRate().divide(new BigDecimal("365"), 8, java.math.RoundingMode.HALF_UP);
        
        switch (calculationMethod) {
            case DAILY:
                return amount.multiply(dailyRate).multiply(new BigDecimal(days));
            case MONTHLY:
                BigDecimal monthlyRate = tier.getInterestRate().divide(new BigDecimal("12"), 8, java.math.RoundingMode.HALF_UP);
                return amount.multiply(monthlyRate).multiply(new BigDecimal(days).divide(new BigDecimal("30"), 8, java.math.RoundingMode.HALF_UP));
            case ANNUALLY:
                return amount.multiply(tier.getInterestRate()).multiply(new BigDecimal(days).divide(new BigDecimal("365"), 8, java.math.RoundingMode.HALF_UP));
            default:
                return amount.multiply(dailyRate).multiply(new BigDecimal(days));
        }
    }
    
    private BigDecimal calculateEffectiveRate(InterestRule rule, BigDecimal amount) {
        if (rule.getIsTiered() && rule.getInterestTiers() != null && !rule.getInterestTiers().isEmpty()) {
            // Calculate weighted average rate for tiered structure
            BigDecimal totalInterest = BigDecimal.ZERO;
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (InterestTier tier : rule.getInterestTiers()) {
                if (tier.getIsActive() && tier.isAmountInTier(amount)) {
                    BigDecimal tierAmount = tier.getTierAmount(amount);
                    totalInterest = totalInterest.add(tierAmount.multiply(tier.getInterestRate()));
                    totalAmount = totalAmount.add(tierAmount);
                }
            }
            
            return totalAmount.compareTo(BigDecimal.ZERO) > 0 ? 
                totalInterest.divide(totalAmount, 8, java.math.RoundingMode.HALF_UP) : 
                rule.getEffectiveRate();
        } else {
            return rule.getEffectiveRate();
        }
    }
    
    public InterestRule approveRule(Long ruleId, String approvedBy) {
        InterestRule rule = interestRuleRepository.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("Interest rule not found"));
        
        rule.setApprovalStatus(InterestRule.ApprovalStatus.APPROVED);
        rule.setApprovedBy(approvedBy);
        rule.setApprovalDate(LocalDateTime.now());
        
        return interestRuleRepository.save(rule);
    }
    
    public InterestRule rejectRule(Long ruleId, String rejectedBy) {
        InterestRule rule = interestRuleRepository.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("Interest rule not found"));
        
        rule.setApprovalStatus(InterestRule.ApprovalStatus.REJECTED);
        rule.setApprovedBy(rejectedBy);
        rule.setApprovalDate(LocalDateTime.now());
        rule.setIsActive(false);
        
        return interestRuleRepository.save(rule);
    }
    
    public void deactivateRule(Long ruleId) {
        InterestRule rule = interestRuleRepository.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("Interest rule not found"));
        
        rule.setIsActive(false);
        interestRuleRepository.save(rule);
    }
    
    public void expireRule(Long ruleId, LocalDateTime expiryDate) {
        InterestRule rule = interestRuleRepository.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("Interest rule not found"));
        
        rule.setExpiryDate(expiryDate);
        if (expiryDate.isBefore(LocalDateTime.now())) {
            rule.setApprovalStatus(InterestRule.ApprovalStatus.EXPIRED);
            rule.setIsActive(false);
        }
        
        interestRuleRepository.save(rule);
    }
    
    public List<InterestRule> getExpiredRules() {
        return interestRuleRepository.findExpiredRules(LocalDateTime.now());
    }
    
    public List<InterestRule> getFutureEffectiveRules() {
        return interestRuleRepository.findFutureEffectiveRules(LocalDateTime.now());
    }
    
    public List<InterestRule> getRulesByCustomerCategory(Long bankId, String customerCategory) {
        return interestRuleRepository.findByBankAndCustomerCategory(bankId, customerCategory);
    }
    
    public List<InterestRule> getRulesByRelationshipTier(Long bankId, String relationshipTier) {
        return interestRuleRepository.findByBankAndRelationshipTier(bankId, relationshipTier);
    }
    
    public List<InterestRule> getShariaCompliantRules(Long bankId) {
        return interestRuleRepository.findByBankIdAndShariaCompliantTrue(bankId);
    }
    
    public InterestRule updateRule(Long ruleId, BigDecimal newBaseRate, BigDecimal newSpread) {
        InterestRule rule = interestRuleRepository.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("Interest rule not found"));
        
        rule.setBaseRate(newBaseRate);
        rule.setSpread(newSpread);
        rule.setApprovalStatus(InterestRule.ApprovalStatus.PENDING); // Require re-approval
        
        return interestRuleRepository.save(rule);
    }
}
