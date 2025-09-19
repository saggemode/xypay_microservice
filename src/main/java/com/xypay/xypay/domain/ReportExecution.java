package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "report_executions")
public class ReportExecution extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;

    @Column(name = "execution_status")
    private String executionStatus; // PENDING, RUNNING, COMPLETED, FAILED

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "executed_by")
    private UUID executedBy;

    @Column(name = "file_path")
    private String filePath; // Path to generated report file

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "rows_processed")
    private Long rowsProcessed;

    @Lob
    @Column(name = "parameters_used")
    private String parametersUsed; // JSON of parameters used for this execution

    @Lob
    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }
}
