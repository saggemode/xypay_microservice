package com.xypay.xypay.repository;

import com.xypay.xypay.domain.InterestRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRuleRepository extends JpaRepository<InterestRule, Long> {
    
    List<InterestRule> findByBankId(Long bankId);
    
    List<InterestRule> findByBankIdAndIsActiveTrue(Long bankId);
    
    Optional<InterestRule> findByRuleCode(String ruleCode);
    
    Optional<InterestRule> findByBankIdAndRuleCode(Long bankId, String ruleCode);
    
    List<InterestRule> findByRuleType(InterestRule.RuleType ruleType);
    
    List<InterestRule> findByBankIdAndRuleType(Long bankId, InterestRule.RuleType ruleType);
    
    List<InterestRule> findByProductCategory(InterestRule.ProductCategory productCategory);
    
    List<InterestRule> findByBankIdAndProductCategory(Long bankId, InterestRule.ProductCategory productCategory);
    
    List<InterestRule> findByCurrencyCode(String currencyCode);
    
    List<InterestRule> findByBankIdAndCurrencyCode(Long bankId, String currencyCode);
    
    List<InterestRule> findByApprovalStatus(InterestRule.ApprovalStatus approvalStatus);
    
    List<InterestRule> findByBankIdAndApprovalStatus(Long bankId, InterestRule.ApprovalStatus approvalStatus);
    
    List<InterestRule> findByShariaCompliantTrue();
    
    List<InterestRule> findByBankIdAndShariaCompliantTrue(Long bankId);
    
    @Query("SELECT ir FROM InterestRule ir WHERE ir.bank.id = :bankId AND ir.isActive = true AND ir.effectiveDate <= :currentDate AND (ir.expiryDate IS NULL OR ir.expiryDate > :currentDate) AND ir.approvalStatus = 'APPROVED'")
    List<InterestRule> findActiveRulesByBank(@Param("bankId") Long bankId, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT ir FROM InterestRule ir WHERE ir.bank.id = :bankId AND ir.ruleType = :ruleType AND ir.productCategory = :productCategory AND ir.isActive = true AND ir.effectiveDate <= :currentDate AND (ir.expiryDate IS NULL OR ir.expiryDate > :currentDate) AND ir.approvalStatus = 'APPROVED'")
    List<InterestRule> findActiveRulesByTypeAndCategory(@Param("bankId") Long bankId, @Param("ruleType") InterestRule.RuleType ruleType, @Param("productCategory") InterestRule.ProductCategory productCategory, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT ir FROM InterestRule ir WHERE ir.bank.id = :bankId AND ir.ruleType = :ruleType AND ir.currencyCode = :currencyCode AND ir.isActive = true AND ir.effectiveDate <= :currentDate AND (ir.expiryDate IS NULL OR ir.expiryDate > :currentDate) AND ir.approvalStatus = 'APPROVED' AND (ir.minimumBalance IS NULL OR ir.minimumBalance <= :amount) AND (ir.maximumBalance IS NULL OR ir.maximumBalance >= :amount)")
    List<InterestRule> findApplicableRules(@Param("bankId") Long bankId, @Param("ruleType") InterestRule.RuleType ruleType, @Param("currencyCode") String currencyCode, @Param("amount") BigDecimal amount, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT ir FROM InterestRule ir WHERE ir.bank.id = :bankId AND ir.customerCategory = :customerCategory AND ir.isActive = true AND ir.approvalStatus = 'APPROVED'")
    List<InterestRule> findByBankAndCustomerCategory(@Param("bankId") Long bankId, @Param("customerCategory") String customerCategory);
    
    @Query("SELECT ir FROM InterestRule ir WHERE ir.bank.id = :bankId AND ir.relationshipTier = :relationshipTier AND ir.isActive = true AND ir.approvalStatus = 'APPROVED'")
    List<InterestRule> findByBankAndRelationshipTier(@Param("bankId") Long bankId, @Param("relationshipTier") String relationshipTier);
    
    @Query("SELECT ir FROM InterestRule ir WHERE ir.expiryDate IS NOT NULL AND ir.expiryDate <= :currentDate AND ir.isActive = true")
    List<InterestRule> findExpiredRules(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT ir FROM InterestRule ir WHERE ir.effectiveDate > :currentDate AND ir.isActive = true")
    List<InterestRule> findFutureEffectiveRules(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT COUNT(ir) FROM InterestRule ir WHERE ir.bank.id = :bankId AND ir.ruleType = :ruleType")
    Long countByBankAndRuleType(@Param("bankId") Long bankId, @Param("ruleType") InterestRule.RuleType ruleType);
    
    @Query("SELECT DISTINCT ir.customerCategory FROM InterestRule ir WHERE ir.bank.id = :bankId AND ir.customerCategory IS NOT NULL")
    List<String> findDistinctCustomerCategoriesByBank(@Param("bankId") Long bankId);
    
    @Query("SELECT DISTINCT ir.relationshipTier FROM InterestRule ir WHERE ir.bank.id = :bankId AND ir.relationshipTier IS NOT NULL")
    List<String> findDistinctRelationshipTiersByBank(@Param("bankId") Long bankId);
}
