package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "workflow_steps")
public class WorkflowStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id")
    private WorkflowDefinition workflowDefinition;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Column(name = "step_order")
    private Integer stepOrder;

    @Column(name = "step_type")
    private String stepType; // APPROVAL, NOTIFICATION, VALIDATION, AUTOMATION

    @Column(name = "approver_role")
    private String approverRole;

    @Column(name = "approver_user_id")
    private Long approverUserId;

    @Column(name = "approval_limit")
    private BigDecimal approvalLimit;

    @Column(name = "is_parallel")
    private Boolean isParallel = false; // Can be processed in parallel with other steps

    @Column(name = "is_mandatory")
    private Boolean isMandatory = true;

    @Column(name = "auto_approve_conditions")
    private String autoApproveConditions; // JSON conditions for auto-approval

    @Column(name = "escalation_timeout_hours")
    private Integer escalationTimeoutHours;

    @Column(name = "escalation_to_role")
    private String escalationToRole;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "workflowStep", cascade = CascadeType.ALL)
    private List<WorkflowStepCondition> conditions;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
