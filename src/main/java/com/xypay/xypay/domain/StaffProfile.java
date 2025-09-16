package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Staff profile entity for tracking staff members who can approve KYC.
 * Equivalent to Django's StaffProfile model.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "staff_profiles", indexes = {
    @Index(name = "idx_staff_role_level", columnList = "role_id, user_id"),
    @Index(name = "idx_staff_employee_id", columnList = "employee_id")
})
public class StaffProfile extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private StaffRole role;
    
    @Column(name = "employee_id", length = 20, unique = true, nullable = false)
    private String employeeId;
    
    @Column(name = "branch", length = 100)
    private String branch;
    
    @Column(name = "department", length = 100)
    private String department;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private StaffProfile supervisor;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;
    
    @Column(name = "last_review_date")
    private LocalDate lastReviewDate;
    
    // Constructors
    public StaffProfile() {}
    
    public StaffProfile(User user, StaffRole role, String employeeId, LocalDate hireDate) {
        this.user = user;
        this.role = role;
        this.employeeId = employeeId;
        this.hireDate = hireDate;
    }
    
    // Business methods
    public boolean canApproveTransaction(BigDecimal amount) {
        return role.canApproveAmount(amount);
    }
    
    public boolean canApproveKyc() {
        return role.getCanApproveKyc();
    }
    
    public boolean canManageStaff() {
        return role.getCanManageStaff();
    }
}
