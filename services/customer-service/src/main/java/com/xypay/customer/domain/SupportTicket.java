package com.xypay.customer.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {
    
    public enum TicketStatus {
        OPEN("open", "Open"),
        IN_PROGRESS("in_progress", "In Progress"),
        PENDING_CUSTOMER("pending_customer", "Pending Customer"),
        RESOLVED("resolved", "Resolved"),
        CLOSED("closed", "Closed"),
        CANCELLED("cancelled", "Cancelled");
        
        private final String value;
        private final String displayName;
        
        TicketStatus(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum TicketPriority {
        LOW("low", "Low"),
        MEDIUM("medium", "Medium"),
        HIGH("high", "High"),
        URGENT("urgent", "Urgent"),
        CRITICAL("critical", "Critical");
        
        private final String value;
        private final String displayName;
        
        TicketPriority(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum TicketCategory {
        ACCOUNT_ISSUES("account_issues", "Account Issues"),
        TRANSACTION_PROBLEMS("transaction_problems", "Transaction Problems"),
        KYC_VERIFICATION("kyc_verification", "KYC Verification"),
        TECHNICAL_SUPPORT("technical_support", "Technical Support"),
        BILLING_INQUIRIES("billing_inquiries", "Billing Inquiries"),
        SECURITY_CONCERNS("security_concerns", "Security Concerns"),
        FEATURE_REQUESTS("feature_requests", "Feature Requests"),
        GENERAL_INQUIRY("general_inquiry", "General Inquiry"),
        COMPLAINT("complaint", "Complaint"),
        OTHER("other", "Other");
        
        private final String value;
        private final String displayName;
        
        TicketCategory(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ticket_number", unique = true, nullable = false)
    private String ticketNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @Column(name = "subject", nullable = false, length = 200)
    private String subject;
    
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private TicketCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private TicketPriority priority = TicketPriority.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status = TicketStatus.OPEN;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id")
    private User assignedAgent;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
    @Column(name = "customer_satisfaction_rating")
    private Integer customerSatisfactionRating; // 1-5 scale
    
    @Column(name = "customer_feedback", columnDefinition = "TEXT")
    private String customerFeedback;
    
    @Column(name = "escalated", nullable = false)
    private Boolean escalated = false;
    
    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;
    
    @Column(name = "escalation_reason", columnDefinition = "TEXT")
    private String escalationReason;
    
    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;
    
    @Column(name = "first_response_time")
    private Long firstResponseTimeMinutes;
    
    @Column(name = "resolution_time")
    private Long resolutionTimeMinutes;
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketComment> comments = new ArrayList<>();
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketAttachment> attachments = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public SupportTicket() {}
    
    public SupportTicket(User customer, String subject, String description, TicketCategory category) {
        this.customer = customer;
        this.subject = subject;
        this.description = description;
        this.category = category;
        this.ticketNumber = generateTicketNumber();
        this.slaDeadline = calculateSLADeadline();
    }
    
    // Business Logic Methods
    private String generateTicketNumber() {
        return "TKT-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
    
    private LocalDateTime calculateSLADeadline() {
        LocalDateTime deadline = LocalDateTime.now();
        switch (this.priority) {
            case CRITICAL:
                deadline = deadline.plusHours(2);
                break;
            case URGENT:
                deadline = deadline.plusHours(4);
                break;
            case HIGH:
                deadline = deadline.plusHours(8);
                break;
            case MEDIUM:
                deadline = deadline.plusDays(1);
                break;
            case LOW:
                deadline = deadline.plusDays(3);
                break;
        }
        return deadline;
    }
    
    public void assignToAgent(User agent) {
        this.assignedAgent = agent;
        this.assignedAt = LocalDateTime.now();
        this.status = TicketStatus.IN_PROGRESS;
    }
    
    public void resolve(String resolutionNotes) {
        this.status = TicketStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = resolutionNotes;
        this.resolutionTimeMinutes = calculateResolutionTime();
    }
    
    public void close() {
        this.status = TicketStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }
    
    public void escalate(String reason) {
        this.escalated = true;
        this.escalatedAt = LocalDateTime.now();
        this.escalationReason = reason;
        this.priority = TicketPriority.URGENT;
        this.slaDeadline = calculateSLADeadline();
    }
    
    public void addCustomerRating(Integer rating, String feedback) {
        this.customerSatisfactionRating = rating;
        this.customerFeedback = feedback;
    }
    
    private Long calculateResolutionTime() {
        if (resolvedAt != null && createdAt != null) {
            return java.time.Duration.between(createdAt, resolvedAt).toMinutes();
        }
        return null;
    }
    
    public boolean isOverdue() {
        return slaDeadline != null && LocalDateTime.now().isAfter(slaDeadline) && 
               !status.equals(TicketStatus.RESOLVED) && !status.equals(TicketStatus.CLOSED);
    }
    
    public boolean isSlaBreached() {
        return isOverdue() && firstResponseTimeMinutes == null;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public TicketCategory getCategory() { return category; }
    public void setCategory(TicketCategory category) { this.category = category; }
    
    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { 
        this.priority = priority; 
        this.slaDeadline = calculateSLADeadline();
    }
    
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    
    public User getAssignedAgent() { return assignedAgent; }
    public void setAssignedAgent(User assignedAgent) { this.assignedAgent = assignedAgent; }
    
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    
    public Integer getCustomerSatisfactionRating() { return customerSatisfactionRating; }
    public void setCustomerSatisfactionRating(Integer customerSatisfactionRating) { this.customerSatisfactionRating = customerSatisfactionRating; }
    
    public String getCustomerFeedback() { return customerFeedback; }
    public void setCustomerFeedback(String customerFeedback) { this.customerFeedback = customerFeedback; }
    
    public Boolean getEscalated() { return escalated; }
    public void setEscalated(Boolean escalated) { this.escalated = escalated; }
    
    public LocalDateTime getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(LocalDateTime escalatedAt) { this.escalatedAt = escalatedAt; }
    
    public String getEscalationReason() { return escalationReason; }
    public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }
    
    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }
    
    public Long getFirstResponseTimeMinutes() { return firstResponseTimeMinutes; }
    public void setFirstResponseTimeMinutes(Long firstResponseTimeMinutes) { this.firstResponseTimeMinutes = firstResponseTimeMinutes; }
    
    public Long getResolutionTimeMinutes() { return resolutionTimeMinutes; }
    public void setResolutionTimeMinutes(Long resolutionTimeMinutes) { this.resolutionTimeMinutes = resolutionTimeMinutes; }
    
    public List<TicketComment> getComments() { return comments; }
    public void setComments(List<TicketComment> comments) { this.comments = comments; }
    
    public List<TicketAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<TicketAttachment> attachments) { this.attachments = attachments; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
