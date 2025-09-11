package com.xypay.xypay.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_alerts", indexes = {
    @Index(name = "idx_security_alert_timestamp", columnList = "timestamp"),
    @Index(name = "idx_security_alert_type_severity", columnList = "alert_type, severity"),
    @Index(name = "idx_security_alert_status", columnList = "status")
})
public class SecurityAlert {
    
    public enum AlertType {
        SUSPICIOUS_LOGIN("suspicious_login"),
        MULTIPLE_FAILED_ATTEMPTS("multiple_failed_attempts"),
        UNUSUAL_ACTIVITY("unusual_activity"),
        SYSTEM_BREACH("system_breach"),
        DATA_LEAK("data_leak"),
        COMPLIANCE_VIOLATION("compliance_violation");
        
        private final String value;
        
        AlertType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    public enum SeverityLevel {
        LOW("low"),
        MEDIUM("medium"),
        HIGH("high"),
        CRITICAL("critical");
        
        private final String value;
        
        SeverityLevel(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    public enum Status {
        OPEN("open"),
        INVESTIGATING("investigating"),
        RESOLVED("resolved"),
        FALSE_POSITIVE("false_positive");
        
        private final String value;
        
        Status(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 50)
    private AlertType alertType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private SeverityLevel severity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.OPEN;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affected_user_id")
    private User affectedUser;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_id")
    private User resolvedBy;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    // Constructors
    public SecurityAlert() {}
    
    public SecurityAlert(AlertType alertType, SeverityLevel severity, String title, 
                        String description, User affectedUser, String ipAddress) {
        this.alertType = alertType;
        this.severity = severity;
        this.title = title;
        this.description = description;
        this.affectedUser = affectedUser;
        this.ipAddress = ipAddress;
        this.status = Status.OPEN;
    }
    
    // Business methods
    public void resolve(User resolvedBy, String notes) {
        this.status = Status.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
        this.notes = notes;
    }
    
    public void markAsFalsePositive(User resolvedBy, String notes) {
        this.status = Status.FALSE_POSITIVE;
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
        this.notes = notes;
    }
    
    public void startInvestigation() {
        this.status = Status.INVESTIGATING;
    }
    
    public boolean isOpen() {
        return this.status == Status.OPEN;
    }
    
    public boolean isResolved() {
        return this.status == Status.RESOLVED || this.status == Status.FALSE_POSITIVE;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public AlertType getAlertType() {
        return alertType;
    }
    
    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }
    
    public SeverityLevel getSeverity() {
        return severity;
    }
    
    public void setSeverity(SeverityLevel severity) {
        this.severity = severity;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public User getAffectedUser() {
        return affectedUser;
    }
    
    public void setAffectedUser(User affectedUser) {
        this.affectedUser = affectedUser;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    public User getResolvedBy() {
        return resolvedBy;
    }
    
    public void setResolvedBy(User resolvedBy) {
        this.resolvedBy = resolvedBy;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s - %s", 
            timestamp, 
            alertType, 
            severity);
    }
}
