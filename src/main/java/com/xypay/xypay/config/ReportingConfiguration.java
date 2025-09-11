package com.xypay.xypay.config;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reporting_configurations")
public class ReportingConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "report_name")
    private String reportName;
    
    @Column(name = "report_type")
    private String reportType; // FINANCIAL, REGULATORY, OPERATIONAL, CUSTOM
    
    @Column(name = "frequency")
    private String frequency; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY, ADHOC
    
    @Column(name = "format")
    private String format; // PDF, EXCEL, CSV, XML, JSON
    
    @Column(name = "recipients")
    private String recipients; // JSON array of email addresses
    
    @Column(name = "parameters")
    private String parameters; // JSON format of report parameters
    
    @Column(name = "scheduled_time")
    private String scheduledTime; // Time in HH:mm format
    
    @Column(name = "dashboard_widgets")
    private String dashboardWidgets; // JSON format of dashboard widget configurations
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}