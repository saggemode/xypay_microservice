package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "workflow_instances")
public class WorkflowInstance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id")
    private WorkflowDefinition workflowDefinition;

    @Column(name = "entity_type")
    private String entityType; // TRANSACTION, LOAN_APPLICATION, KYC_DOCUMENT, etc.

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "current_step_id")
    private UUID currentStepId;

    @Column(name = "status")
    private String status; // PENDING, IN_PROGRESS, APPROVED, REJECTED, CANCELLED

    @Column(name = "initiated_by")
    private UUID initiatedBy;

    @Column(name = "priority")
    private String priority; // HIGH, MEDIUM, LOW

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Lob
    @Column(name = "context_data")
    private String contextData; // JSON data for workflow context

    @OneToMany(mappedBy = "workflowInstance", cascade = CascadeType.ALL)
    private List<WorkflowInstanceStep> instanceSteps;

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
