package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_name", nullable = false)
    private String reportName;

    @Column(name = "report_type")
    private String reportType; // REGULATORY, MANAGEMENT, OPERATIONAL, COMPLIANCE

    @Column(name = "report_category")
    private String reportCategory; // DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUAL, AD_HOC

    @Column(name = "description")
    private String description;

    @Lob
    @Column(name = "sql_query")
    private String sqlQuery;

    @Lob
    @Column(name = "parameters_config")
    private String parametersConfig; // JSON configuration for parameters

    @Column(name = "output_format")
    private String outputFormat; // PDF, EXCEL, CSV, JSON

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_scheduled")
    private Boolean isScheduled = false;

    @Column(name = "schedule_cron")
    private String scheduleCron; // Cron expression for scheduled reports

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<ReportExecution> executions;

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
