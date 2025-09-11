package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "workflow_step_conditions")
public class WorkflowStepCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_id")
    private WorkflowStep workflowStep;

    @Column(name = "condition_type")
    private String conditionType; // AMOUNT_RANGE, USER_ROLE, TIME_BASED, CUSTOM

    @Column(name = "condition_field")
    private String conditionField; // amount, userRole, createdDate, etc.

    @Column(name = "condition_operator")
    private String conditionOperator; // GT, LT, EQ, IN, BETWEEN

    @Column(name = "condition_value")
    private String conditionValue;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
