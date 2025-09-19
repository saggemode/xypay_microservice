package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "workflow_instance_steps")
public class WorkflowInstanceStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_instance_id")
    private WorkflowInstance workflowInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_id")
    private WorkflowStep workflowStep;

    @Column(name = "status")
    private String status; // PENDING, IN_PROGRESS, APPROVED, REJECTED, SKIPPED

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "assigned_role")
    private String assignedRole;

    @Column(name = "action_taken_by")
    private UUID actionTakenBy;

    @Column(name = "action_date")
    private LocalDateTime actionDate;

    @Column(name = "comments")
    private String comments;

    @Column(name = "escalated")
    private Boolean escalated = false;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "escalated_to")
    private UUID escalatedTo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
