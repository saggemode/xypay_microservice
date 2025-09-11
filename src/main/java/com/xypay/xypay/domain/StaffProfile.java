package com.xypay.xypay.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;


/**
 * Staff profile entity for tracking staff members who can approve KYC.
 * Equivalent to Django's StaffProfile model.
 */
@Entity
@Table(name = "staff_profiles")
public class StaffProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "employee_id", unique = true)
    private String employeeId;
    
    @Column(name = "department")
    private String department;
    
    @Column(name = "position")
    private String position;
    
    @Column(name = "can_approve_kyc", nullable = false)
    private Boolean canApproveKyc = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public StaffProfile() {
        this.createdAt = LocalDateTime.now();
    }
    
    public StaffProfile(User user, String employeeId) {
        this();
        this.user = user;
        this.employeeId = employeeId;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public Boolean getCanApproveKyc() {
        return canApproveKyc;
    }
    
    public void setCanApproveKyc(Boolean canApproveKyc) {
        this.canApproveKyc = canApproveKyc;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
