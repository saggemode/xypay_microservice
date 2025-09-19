package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@Entity
@Table(name = "workflow_definitions")
@EqualsAndHashCode(callSuper = true)
public class WorkflowDefinition extends BaseEntity {
    

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(name = "workflow_type")
    private String workflowType; // TRANSACTION_APPROVAL, LOAN_APPROVAL, KYC_APPROVAL, etc.

    @Lob
    @Column(name = "config_json")
    private String configJson;

    private String owner;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "version")
    private Integer version = 1;

    @OneToMany(mappedBy = "workflowDefinition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkflowStep> steps;
    
}
