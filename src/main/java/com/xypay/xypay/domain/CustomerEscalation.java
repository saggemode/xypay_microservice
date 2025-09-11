package com.xypay.xypay.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "customer_escalations", indexes = {
    @Index(name = "idx_escalation_created_by", columnList = "created_by_id"),
    @Index(name = "idx_escalation_assigned_to", columnList = "assigned_to_id"),
    @Index(name = "idx_escalation_status", columnList = "status"),
    @Index(name = "idx_escalation_priority", columnList = "priority")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CustomerEscalation extends BaseEntity {

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "priority", nullable = false, length = 20)
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "status", nullable = false, length = 20)
    private String status = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    @Column(name = "category", length = 50)
    private String category; // TECHNICAL, BILLING, ACCOUNT, TRANSACTION, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer; // The customer this escalation is about

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    @Column(name = "reference_number", unique = true, length = 50)
    private String referenceNumber;

    @Column(name = "source", length = 20)
    private String source = "MANUAL"; // MANUAL, SYSTEM, API, COMPLAINT

    @Column(name = "severity_level")
    private Integer severityLevel = 1; // 1-5 scale

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "escalated_from", length = 100)
    private String escalatedFrom; // What triggered this escalation

    // Constructors
    public CustomerEscalation() {
        super();
    }

    public CustomerEscalation(String subject, String description, User createdBy) {
        this();
        this.subject = subject;
        this.description = description;
        this.createdBy = createdBy;
        this.generateReferenceNumber();
    }

    // Note: getId() and setId() are inherited from BaseEntity

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(Integer severityLevel) {
        this.severityLevel = severityLevel;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public String getEscalatedFrom() {
        return escalatedFrom;
    }

    public void setEscalatedFrom(String escalatedFrom) {
        this.escalatedFrom = escalatedFrom;
    }

    // Business methods
    public void markAsResolved(String resolutionNotes) {
        this.status = "RESOLVED";
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = resolutionNotes;
    }

    public void markAsClosed(String closingNotes) {
        this.status = "CLOSED";
        this.closedAt = LocalDateTime.now();
        if (this.resolutionNotes == null || this.resolutionNotes.isEmpty()) {
            this.resolutionNotes = closingNotes;
        }
    }

    public void assignTo(User user) {
        this.assignedTo = user;
        if ("OPEN".equals(this.status)) {
            this.status = "IN_PROGRESS";
        }
    }

    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && 
               !"RESOLVED".equals(status) && !"CLOSED".equals(status);
    }

    public boolean isHighPriority() {
        return "HIGH".equals(priority) || "CRITICAL".equals(priority);
    }

    private void generateReferenceNumber() {
        if (this.referenceNumber == null) {
            this.referenceNumber = "ESC-" + System.currentTimeMillis();
        }
    }

    @PrePersist
    protected void onCreate() {
        generateReferenceNumber();
    }

    @Override
    public String toString() {
        return "CustomerEscalation{" +
                "id=" + getId() +
                ", subject='" + subject + '\'' +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                ", referenceNumber='" + referenceNumber + '\'' +
                '}';
    }
}
