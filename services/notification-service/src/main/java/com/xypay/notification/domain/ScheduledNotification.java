package com.xypay.notification.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_notifications")
public class ScheduledNotification {
    
    public enum ScheduleStatus {
        PENDING("pending", "Pending"),
        SCHEDULED("scheduled", "Scheduled"),
        SENT("sent", "Sent"),
        FAILED("failed", "Failed"),
        CANCELLED("cancelled", "Cancelled");
        
        private final String value;
        private final String displayName;
        
        ScheduleStatus(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum ScheduleType {
        IMMEDIATE("immediate", "Immediate"),
        DELAYED("delayed", "Delayed"),
        RECURRING("recurring", "Recurring"),
        CONDITIONAL("conditional", "Conditional");
        
        private final String value;
        private final String displayName;
        
        ScheduleType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false)
    private ScheduleType scheduleType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScheduleStatus status = ScheduleStatus.PENDING;
    
    @Column(name = "scheduled_for", nullable = false)
    private LocalDateTime scheduledFor;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "max_retries")
    private Integer maxRetries = 3;
    
    @Column(name = "retry_interval_minutes")
    private Integer retryIntervalMinutes = 5;
    
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;
    
    @Column(name = "recurrence_pattern")
    private String recurrencePattern; // CRON expression for recurring notifications
    
    @Column(name = "condition_expression")
    private String conditionExpression; // For conditional notifications
    
    @Column(name = "template_key")
    private String templateKey;
    
    @Column(name = "template_variables", columnDefinition = "TEXT")
    private String templateVariables; // JSON string
    
    @Column(name = "channels", columnDefinition = "TEXT")
    private String channels; // JSON array of channels to send to
    
    @Column(name = "priority")
    private Integer priority = 0;
    
    @Column(name = "source")
    private String source;
    
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public ScheduledNotification() {}
    
    public ScheduledNotification(Long userId, String title, String message, 
                               NotificationType notificationType, LocalDateTime scheduledFor) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.scheduledFor = scheduledFor;
        this.scheduleType = ScheduleType.DELAYED;
    }
    
    // Business Logic Methods
    public boolean isReadyToSend() {
        return status == ScheduleStatus.SCHEDULED && 
               LocalDateTime.now().isAfter(scheduledFor);
    }
    
    public boolean canRetry() {
        return status == ScheduleStatus.FAILED && 
               retryCount < maxRetries &&
               (nextRetryAt == null || LocalDateTime.now().isAfter(nextRetryAt));
    }
    
    public void markAsSent() {
        this.status = ScheduleStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String reason) {
        this.status = ScheduleStatus.FAILED;
        this.failureReason = reason;
        this.retryCount++;
        
        if (canRetry()) {
            this.nextRetryAt = LocalDateTime.now().plusMinutes(retryIntervalMinutes);
        }
    }
    
    public void scheduleRetry() {
        if (canRetry()) {
            this.status = ScheduleStatus.SCHEDULED;
            this.scheduledFor = nextRetryAt;
            this.nextRetryAt = null;
        }
    }
    
    public void cancel() {
        this.status = ScheduleStatus.CANCELLED;
    }
    
    public boolean isRecurring() {
        return scheduleType == ScheduleType.RECURRING && 
               recurrencePattern != null && !recurrencePattern.isEmpty();
    }
    
    public boolean isConditional() {
        return scheduleType == ScheduleType.CONDITIONAL && 
               conditionExpression != null && !conditionExpression.isEmpty();
    }
    
    public boolean isOverdue() {
        return status == ScheduleStatus.SCHEDULED && 
               LocalDateTime.now().isAfter(scheduledFor.plusMinutes(30));
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }
    
    public ScheduleType getScheduleType() { return scheduleType; }
    public void setScheduleType(ScheduleType scheduleType) { this.scheduleType = scheduleType; }
    
    public ScheduleStatus getStatus() { return status; }
    public void setStatus(ScheduleStatus status) { this.status = status; }
    
    public LocalDateTime getScheduledFor() { return scheduledFor; }
    public void setScheduledFor(LocalDateTime scheduledFor) { this.scheduledFor = scheduledFor; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    
    public Integer getMaxRetries() { return maxRetries; }
    public void setMaxRetries(Integer maxRetries) { this.maxRetries = maxRetries; }
    
    public Integer getRetryIntervalMinutes() { return retryIntervalMinutes; }
    public void setRetryIntervalMinutes(Integer retryIntervalMinutes) { this.retryIntervalMinutes = retryIntervalMinutes; }
    
    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(LocalDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }
    
    public String getRecurrencePattern() { return recurrencePattern; }
    public void setRecurrencePattern(String recurrencePattern) { this.recurrencePattern = recurrencePattern; }
    
    public String getConditionExpression() { return conditionExpression; }
    public void setConditionExpression(String conditionExpression) { this.conditionExpression = conditionExpression; }
    
    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }
    
    public String getTemplateVariables() { return templateVariables; }
    public void setTemplateVariables(String templateVariables) { this.templateVariables = templateVariables; }
    
    public String getChannels() { return channels; }
    public void setChannels(String channels) { this.channels = channels; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
