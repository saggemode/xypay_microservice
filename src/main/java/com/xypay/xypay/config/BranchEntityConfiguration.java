package com.xypay.xypay.config;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "branch_entity_configurations")
public class BranchEntityConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "branch_code")
    private String branchCode;
    
    @Column(name = "branch_name")
    private String branchName;
    
    @Column(name = "entity_type")
    private String entityType; // BRANCH, ENTITY, COUNTRY
    
    @Column(name = "parent_entity_id")
    private UUID parentEntityId;
    
    @Column(name = "country_code")
    private String countryCode;
    
    @Column(name = "currency")
    private String currency;
    
    @Column(name = "gl_account_prefix")
    private String glAccountPrefix;
    
    @Column(name = "regional_holidays")
    private String regionalHolidays; // JSON array of holiday dates
    
    @Column(name = "working_hours")
    private String workingHours; // JSON format of working hours
    
    @Column(name = "compliance_rules")
    private String complianceRules; // JSON format of country-specific compliance rules
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}