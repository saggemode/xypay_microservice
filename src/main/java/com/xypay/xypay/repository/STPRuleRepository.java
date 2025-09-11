package com.xypay.xypay.repository;

import com.xypay.xypay.domain.STPRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface STPRuleRepository extends JpaRepository<STPRule, Long> {
    
    List<STPRule> findByEntityTypeAndIsActiveTrueOrderByPriorityDesc(String entityType);
    
    List<STPRule> findByRuleTypeAndIsActiveTrueOrderByPriorityDesc(String ruleType);
    
    @Query("SELECT r FROM STPRule r WHERE r.entityType = :entityType AND r.isActive = true ORDER BY r.priority DESC, r.id ASC")
    List<STPRule> findApplicableRules(@Param("entityType") String entityType);
    
    @Query("SELECT r FROM STPRule r WHERE r.autoApprove = true AND r.entityType = :entityType AND r.isActive = true ORDER BY r.priority DESC")
    List<STPRule> findAutoApprovalRules(@Param("entityType") String entityType);
}
