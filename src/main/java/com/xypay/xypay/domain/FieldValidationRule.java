package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "field_validation_rules")
public class FieldValidationRule extends BaseEntity {
    
    @Column(name = "rule_code", unique = true, nullable = false)
    private String ruleCode;
    
    @Column(name = "rule_name", nullable = false)
    private String ruleName;
    
    @Column(name = "field_name", nullable = false)
    private String fieldName;
    
    @Column(name = "screen_code")
    private String screenCode;
    
    @Column(name = "validation_type", nullable = false)
    private String validationType; // REQUIRED, REGEX, RANGE, CUSTOM, LOOKUP, CROSS_FIELD
    
    @Column(name = "validation_expression", columnDefinition = "TEXT")
    private String validationExpression; // Regex, script, or condition
    
    @Column(name = "error_message", nullable = false)
    private String errorMessage;
    
    @Column(name = "warning_message")
    private String warningMessage;
    
    @Column(name = "validation_level")
    private String validationLevel = "ERROR"; // ERROR, WARNING, INFO
    
    @Column(name = "execution_order")
    private Integer executionOrder = 1;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_required")
    private Boolean isRequired = false;
    
    @Column(name = "applicable_roles", columnDefinition = "TEXT")
    private String applicableRoles; // JSON array of roles this rule applies to
    
    @Column(name = "bypass_roles", columnDefinition = "TEXT")
    private String bypassRoles; // JSON array of roles that can bypass this rule
    
    @Column(name = "conditions", columnDefinition = "TEXT")
    private String conditions; // Additional conditions for validation
    
    @Column(name = "validation_context", columnDefinition = "TEXT")
    private String validationContext; // Additional context for validation
}