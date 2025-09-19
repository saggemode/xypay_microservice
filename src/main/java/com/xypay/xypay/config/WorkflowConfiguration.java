package com.xypay.xypay.config;

import com.xypay.xypay.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "workflow_configurations")
public class WorkflowConfiguration extends BaseEntity {
    
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
    
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}