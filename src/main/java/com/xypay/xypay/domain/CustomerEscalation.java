package com.xypay.xypay.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customer_escalations", indexes = {
    @Index(name = "idx_escalation_priority_created", columnList = "priority, created_at"),
    @Index(name = "idx_escalation_customer", columnList = "customer_id"),
    @Index(name = "idx_escalation_status", columnList = "status")
})
public class CustomerEscalation extends BaseEntity {
    
    public enum Priority {
        LOW("low", "Low"),
        MEDIUM("medium", "Medium"),
        HIGH("high", "High"),
        URGENT("urgent", "Urgent");
        
        private final String code;
        private final String displayName;
        
        Priority(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum EscalationStatus {
        OPEN("open", "Open"),
        IN_PROGRESS("in_progress", "In Progress"),
        RESOLVED("resolved", "Resolved"),
        CLOSED("closed", "Closed");
        
        private final String code;
        private final String displayName;
        
        EscalationStatus(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private StaffProfile createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private StaffProfile assignedTo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20, nullable = false)
    private Priority priority = Priority.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private EscalationStatus status = EscalationStatus.OPEN;
    
    @Column(name = "subject", length = 200, nullable = false)
    private String subject;
    
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;
    
    @Column(name = "resolved_at")
    private java.time.LocalDateTime resolvedAt;
    
    // Constructors
    public CustomerEscalation() {}
    
    public CustomerEscalation(User customer, StaffProfile createdBy, String subject, String description) {
        this.customer = customer;
        this.createdBy = createdBy;
        this.subject = subject;
        this.description = description;
    }
    
    // Business methods
    public void assignTo(StaffProfile staff) {
        this.assignedTo = staff;
        this.status = EscalationStatus.IN_PROGRESS;
    }
    
    public void resolve(String resolution) {
        this.status = EscalationStatus.RESOLVED;
        this.resolution = resolution;
        this.resolvedAt = java.time.LocalDateTime.now();
    }
    
    public void close() {
        this.status = EscalationStatus.CLOSED;
    }
}