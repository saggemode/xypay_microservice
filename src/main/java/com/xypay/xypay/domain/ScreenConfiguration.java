package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "screen_configurations")
public class ScreenConfiguration extends BaseEntity {
    
    @Column(name = "screen_code", unique = true, nullable = false)
    private String screenCode;
    
    @Column(name = "screen_name", nullable = false)
    private String screenName;
    
    @Column(name = "screen_type")
    private String screenType; // FORM, LIST, DASHBOARD, REPORT
    
    @Column(name = "layout_config", columnDefinition = "TEXT")
    private String layoutConfig; // JSON configuration for layout
    
    @Column(name = "field_config", columnDefinition = "TEXT")
    private String fieldConfig; // JSON configuration for fields
    
    @Column(name = "validation_rules", columnDefinition = "TEXT")
    private String validationRules; // JSON validation rules
    
    @Column(name = "access_roles", columnDefinition = "TEXT")
    private String accessRoles; // JSON array of roles
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "version")
    private Integer version = 1;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "approval_status")
    private String approvalStatus = "DRAFT"; // DRAFT, PENDING, APPROVED, REJECTED
}
