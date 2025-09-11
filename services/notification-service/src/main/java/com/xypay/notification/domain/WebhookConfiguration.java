package com.xypay.notification.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_configurations")
public class WebhookConfiguration {
    
    public enum WebhookStatus {
        ACTIVE("active", "Active"),
        INACTIVE("inactive", "Inactive"),
        SUSPENDED("suspended", "Suspended");
        
        private final String value;
        private final String displayName;
        
        WebhookStatus(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum WebhookEvent {
        NOTIFICATION_SENT("notification_sent", "Notification Sent"),
        NOTIFICATION_DELIVERED("notification_delivered", "Notification Delivered"),
        NOTIFICATION_READ("notification_read", "Notification Read"),
        NOTIFICATION_FAILED("notification_failed", "Notification Failed"),
        NOTIFICATION_BOUNCED("notification_bounced", "Notification Bounced"),
        USER_SUBSCRIBED("user_subscribed", "User Subscribed"),
        USER_UNSUBSCRIBED("user_unsubscribed", "User Unsubscribed"),
        CAMPAIGN_STARTED("campaign_started", "Campaign Started"),
        CAMPAIGN_COMPLETED("campaign_completed", "Campaign Completed");
        
        private final String value;
        private final String displayName;
        
        WebhookEvent(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "url", nullable = false, length = 500)
    private String url;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WebhookStatus status = WebhookStatus.ACTIVE;
    
    @Column(name = "events", columnDefinition = "TEXT")
    private String events; // JSON array of events
    
    @Column(name = "secret_key")
    private String secretKey;
    
    @Column(name = "headers", columnDefinition = "TEXT")
    private String headers; // JSON object of custom headers
    
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds = 30;
    
    @Column(name = "retry_attempts")
    private Integer retryAttempts = 3;
    
    @Column(name = "retry_delay_seconds")
    private Integer retryDelaySeconds = 5;
    
    @Column(name = "is_ssl_verified", nullable = false)
    private Boolean isSslVerified = true;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;
    
    @Column(name = "success_count")
    private Long successCount = 0L;
    
    @Column(name = "failure_count")
    private Long failureCount = 0L;
    
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public WebhookConfiguration() {}
    
    public WebhookConfiguration(String name, String url, String events) {
        this.name = name;
        this.url = url;
        this.events = events;
    }
    
    // Business Logic Methods
    public void markAsTriggered() {
        this.lastTriggeredAt = LocalDateTime.now();
    }
    
    public void incrementSuccessCount() {
        this.successCount++;
    }
    
    public void incrementFailureCount() {
        this.failureCount++;
    }
    
    public void setLastError(String error) {
        this.lastError = error;
        this.failureCount++;
    }
    
    public void clearLastError() {
        this.lastError = null;
    }
    
    public boolean isHealthy() {
        return failureCount == 0 || (successCount > 0 && failureCount < successCount);
    }
    
    public double getSuccessRate() {
        long total = successCount + failureCount;
        return total > 0 ? (successCount * 100.0) / total : 0.0;
    }
    
    public boolean shouldRetry() {
        return retryAttempts > 0 && failureCount < retryAttempts;
    }
    
    public void activate() {
        this.status = WebhookStatus.ACTIVE;
        this.isActive = true;
    }
    
    public void deactivate() {
        this.status = WebhookStatus.INACTIVE;
        this.isActive = false;
    }
    
    public void suspend() {
        this.status = WebhookStatus.SUSPENDED;
        this.isActive = false;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public WebhookStatus getStatus() { return status; }
    public void setStatus(WebhookStatus status) { this.status = status; }
    
    public String getEvents() { return events; }
    public void setEvents(String events) { this.events = events; }
    
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    
    public String getHeaders() { return headers; }
    public void setHeaders(String headers) { this.headers = headers; }
    
    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    
    public Integer getRetryAttempts() { return retryAttempts; }
    public void setRetryAttempts(Integer retryAttempts) { this.retryAttempts = retryAttempts; }
    
    public Integer getRetryDelaySeconds() { return retryDelaySeconds; }
    public void setRetryDelaySeconds(Integer retryDelaySeconds) { this.retryDelaySeconds = retryDelaySeconds; }
    
    public Boolean getIsSslVerified() { return isSslVerified; }
    public void setIsSslVerified(Boolean isSslVerified) { this.isSslVerified = isSslVerified; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getLastTriggeredAt() { return lastTriggeredAt; }
    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) { this.lastTriggeredAt = lastTriggeredAt; }
    
    public Long getSuccessCount() { return successCount; }
    public void setSuccessCount(Long successCount) { this.successCount = successCount; }
    
    public Long getFailureCount() { return failureCount; }
    public void setFailureCount(Long failureCount) { this.failureCount = failureCount; }
    
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
