package com.xypay.xypay.config;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "workflow_configurations")
public class WorkflowConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "workflow_name")
    private String workflowName;
    
    @Column(name = "process_type")
    private String processType; // CUSTOMER_ONBOARDING, LOAN_APPROVAL, TRANSACTION_APPROVAL
    
    @Column(name = "steps")
    private String steps; // JSON format of workflow steps
    
    @Column(name = "field_access_control")
    private String fieldAccessControl; // JSON format of field-level access control rules
    
    @Column(name = "role_permissions")
    private String rolePermissions; // JSON format of role-based permissions
    
    @Column(name = "ui_customization")
    private String uiCustomization; // JSON format of UI customization rules
    
    @Column(name = "language_settings")
    private String languageSettings; // JSON format of multi-lingual settings
    
    @Column(name = "menu_navigation")
    private String menuNavigation; // JSON format of menu and navigation personalization
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}