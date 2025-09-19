package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "staff_roles")
public class StaffRole extends BaseEntity {
    
    public enum RoleType {
        TELLER("teller", "Teller"),
        CUSTOMER_SERVICE("customer_service", "Customer Service Representative"),
        PERSONAL_BANKER("personal_banker", "Personal Banker"),
        ASSISTANT_MANAGER("assistant_manager", "Assistant Manager"),
        MANAGER("manager", "Manager"),
        BRANCH_MANAGER("branch_manager", "Branch Manager");
        
        private final String code;
        private final String displayName;
        
        RoleType(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @Enumerated(EnumType.STRING)
    @Column(name = "name", length = 50, unique = true, nullable = false)
    private RoleType name;
    
    @Column(name = "level", nullable = false)
    private Integer level;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "max_transaction_approval", precision = 19, scale = 4, nullable = false)
    private BigDecimal maxTransactionApproval = BigDecimal.ZERO;
    
    @Column(name = "can_approve_kyc", nullable = false)
    private Boolean canApproveKyc = false;
    
    @Column(name = "can_manage_staff", nullable = false)
    private Boolean canManageStaff = false;
    
    @Column(name = "can_view_reports", nullable = false)
    private Boolean canViewReports = false;
    
    @Column(name = "can_override_transactions", nullable = false)
    private Boolean canOverrideTransactions = false;
    
    @Column(name = "can_handle_escalations", nullable = false)
    private Boolean canHandleEscalations = false;
    
    // Constructors
    public StaffRole() {}
    
    public StaffRole(RoleType name, Integer level, String description, BigDecimal maxTransactionApproval) {
        this.name = name;
        this.level = level;
        this.description = description;
        this.maxTransactionApproval = maxTransactionApproval;
    }
    
    // Business methods
    public boolean canApproveAmount(BigDecimal amount) {
        return amount.compareTo(maxTransactionApproval) <= 0;
    }
    
    // Getter methods that were missing
    public Boolean getCanApproveKyc() {
        return canApproveKyc;
    }
    
    public Boolean getCanManageStaff() {
        return canManageStaff;
    }
}