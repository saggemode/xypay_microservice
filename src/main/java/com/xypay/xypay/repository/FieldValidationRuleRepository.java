package com.xypay.xypay.repository;

import com.xypay.xypay.domain.FieldValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FieldValidationRuleRepository extends JpaRepository<FieldValidationRule, Long> {
    Optional<FieldValidationRule> findByRuleCode(String ruleCode);
    List<FieldValidationRule> findByScreenCode(String screenCode);
    List<FieldValidationRule> findByFieldName(String fieldName);
    List<FieldValidationRule> findByValidationType(String validationType);
    List<FieldValidationRule> findByIsActive(Boolean isActive);
    List<FieldValidationRule> findByScreenCodeAndFieldNameOrderByExecutionOrder(String screenCode, String fieldName);
    List<FieldValidationRule> findByScreenCodeAndIsActiveOrderByExecutionOrder(String screenCode, Boolean isActive);
}
