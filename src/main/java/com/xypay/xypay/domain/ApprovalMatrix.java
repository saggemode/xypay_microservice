package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "approval_matrix")
public class ApprovalMatrix extends BaseEntity {
    
    @Column(name = "matrix_code", unique = true, nullable = false)
    private String matrixCode;
    
    @Column(name = "matrix_name", nullable = false)
    private String matrixName;
    
    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // TRANSFER, LOAN, ACCOUNT_OPENING, etc.
    
    @Column(name = "product_type")
    private String productType;
    
    @Column(name = "branch_code")
    private String branchCode;
    
    @Column(name = "customer_type")
    private String customerType;
    
    @Column(name = "amount_from", precision = 19, scale = 4)
    private BigDecimal amountFrom;
    
    @Column(name = "amount_to", precision = 19, scale = 4)
    private BigDecimal amountTo;
    
    @Column(name = "currency")
    private String currency;
    
    @Column(name = "approval_level", nullable = false)
    private Integer approvalLevel;
    
    @Column(name = "required_role", nullable = false)
    private String requiredRole;
    
    @Column(name = "alternative_roles", columnDefinition = "TEXT")
    private String alternativeRoles; // JSON array of alternative roles
    
    @Column(name = "is_mandatory")
    private Boolean isMandatory = true;
    
    @Column(name = "can_self_approve")
    private Boolean canSelfApprove = false;
    
    @Column(name = "timeout_hours")
    private Integer timeoutHours;
    
    @Column(name = "escalation_role")
    private String escalationRole;
    
    @Column(name = "approval_conditions", columnDefinition = "TEXT")
    private String approvalConditions; // JSON conditions for approval
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "effective_from")
    private java.time.LocalDateTime effectiveFrom;
    
    @Column(name = "effective_to")
    private java.time.LocalDateTime effectiveTo;
}
