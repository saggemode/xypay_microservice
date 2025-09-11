package com.xypay.customer.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_feedback")
public class CustomerFeedback {
    
    public enum FeedbackType {
        GENERAL("general", "General Feedback"),
        SERVICE_QUALITY("service_quality", "Service Quality"),
        APP_EXPERIENCE("app_experience", "App Experience"),
        SUPPORT_EXPERIENCE("support_experience", "Support Experience"),
        FEATURE_REQUEST("feature_request", "Feature Request"),
        BUG_REPORT("bug_report", "Bug Report"),
        COMPLAINT("complaint", "Complaint"),
        COMPLIMENT("compliment", "Compliment");
        
        private final String value;
        private final String displayName;
        
        FeedbackType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum FeedbackStatus {
        SUBMITTED("submitted", "Submitted"),
        UNDER_REVIEW("under_review", "Under Review"),
        ACKNOWLEDGED("acknowledged", "Acknowledged"),
        IN_PROGRESS("in_progress", "In Progress"),
        RESOLVED("resolved", "Resolved"),
        CLOSED("closed", "Closed");
        
        private final String value;
        private final String displayName;
        
        FeedbackStatus(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false)
    private FeedbackType feedbackType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FeedbackStatus status = FeedbackStatus.SUBMITTED;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(name = "rating")
    private Integer rating; // 1-5 scale
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "tags")
    private String tags; // Comma-separated tags
    
    @Column(name = "priority")
    private String priority; // Low, Medium, High, Critical
    
    @Column(name = "assigned_to")
    private String assignedTo; // Staff member assigned to handle feedback
    
    @Column(name = "response")
    private String response; // Staff response to feedback
    
    @Column(name = "response_date")
    private LocalDateTime responseDate;
    
    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;
    
    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = false;
    
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "device_info")
    private String deviceInfo;
    
    @Column(name = "app_version")
    private String appVersion;
    
    @Column(name = "os_version")
    private String osVersion;
    
    @Column(name = "browser_info")
    private String browserInfo;
    
    @Column(name = "follow_up_required", nullable = false)
    private Boolean followUpRequired = false;
    
    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;
    
    @Column(name = "escalated", nullable = false)
    private Boolean escalated = false;
    
    @Column(name = "escalation_reason")
    private String escalationReason;
    
    @Column(name = "escalated_to")
    private String escalatedTo;
    
    @Column(name = "escalated_date")
    private LocalDateTime escalatedDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public CustomerFeedback() {}
    
    public CustomerFeedback(User customer, FeedbackType feedbackType, String title, String description) {
        this.customer = customer;
        this.feedbackType = feedbackType;
        this.title = title;
        this.description = description;
    }
    
    // Business Logic Methods
    public void assignTo(String staffMember) {
        this.assignedTo = staffMember;
        this.status = FeedbackStatus.UNDER_REVIEW;
    }
    
    public void acknowledge(String response) {
        this.response = response;
        this.responseDate = LocalDateTime.now();
        this.status = FeedbackStatus.ACKNOWLEDGED;
    }
    
    public void resolve(String response) {
        this.response = response;
        this.resolvedDate = LocalDateTime.now();
        this.status = FeedbackStatus.RESOLVED;
    }
    
    public void close() {
        this.status = FeedbackStatus.CLOSED;
    }
    
    public void escalate(String reason, String escalatedTo) {
        this.escalated = true;
        this.escalationReason = reason;
        this.escalatedTo = escalatedTo;
        this.escalatedDate = LocalDateTime.now();
        this.priority = "High";
    }
    
    public void scheduleFollowUp(LocalDateTime followUpDate) {
        this.followUpRequired = true;
        this.followUpDate = followUpDate;
    }
    
    public boolean isOverdue() {
        return followUpRequired && followUpDate != null && LocalDateTime.now().isAfter(followUpDate);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    
    public FeedbackType getFeedbackType() { return feedbackType; }
    public void setFeedbackType(FeedbackType feedbackType) { this.feedbackType = feedbackType; }
    
    public FeedbackStatus getStatus() { return status; }
    public void setStatus(FeedbackStatus status) { this.status = status; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    
    public LocalDateTime getResponseDate() { return responseDate; }
    public void setResponseDate(LocalDateTime responseDate) { this.responseDate = responseDate; }
    
    public LocalDateTime getResolvedDate() { return resolvedDate; }
    public void setResolvedDate(LocalDateTime resolvedDate) { this.resolvedDate = resolvedDate; }
    
    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }
    
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    
    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
    
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    
    public String getBrowserInfo() { return browserInfo; }
    public void setBrowserInfo(String browserInfo) { this.browserInfo = browserInfo; }
    
    public Boolean getFollowUpRequired() { return followUpRequired; }
    public void setFollowUpRequired(Boolean followUpRequired) { this.followUpRequired = followUpRequired; }
    
    public LocalDateTime getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDateTime followUpDate) { this.followUpDate = followUpDate; }
    
    public Boolean getEscalated() { return escalated; }
    public void setEscalated(Boolean escalated) { this.escalated = escalated; }
    
    public String getEscalationReason() { return escalationReason; }
    public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }
    
    public String getEscalatedTo() { return escalatedTo; }
    public void setEscalatedTo(String escalatedTo) { this.escalatedTo = escalatedTo; }
    
    public LocalDateTime getEscalatedDate() { return escalatedDate; }
    public void setEscalatedDate(LocalDateTime escalatedDate) { this.escalatedDate = escalatedDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
