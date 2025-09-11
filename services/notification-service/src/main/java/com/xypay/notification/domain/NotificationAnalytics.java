package com.xypay.notification.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_analytics")
public class NotificationAnalytics {
    
    public enum DeliveryChannel {
        EMAIL("email", "Email"),
        SMS("sms", "SMS"),
        PUSH("push", "Push Notification"),
        IN_APP("in_app", "In-App Notification"),
        WEBHOOK("webhook", "Webhook");
        
        private final String value;
        private final String displayName;
        
        DeliveryChannel(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum DeliveryStatus {
        PENDING("pending", "Pending"),
        SENT("sent", "Sent"),
        DELIVERED("delivered", "Delivered"),
        READ("read", "Read"),
        FAILED("failed", "Failed"),
        BOUNCED("bounced", "Bounced"),
        UNSUBSCRIBED("unsubscribed", "Unsubscribed");
        
        private final String value;
        private final String displayName;
        
        DeliveryStatus(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "notification_id", nullable = false)
    private Long notificationId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private DeliveryChannel channel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status = DeliveryStatus.PENDING;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    
    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;
    
    @Column(name = "delivery_attempts")
    private Integer deliveryAttempts = 0;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @Column(name = "template_key")
    private String templateKey;
    
    @Column(name = "campaign_id")
    private String campaignId;
    
    @Column(name = "segment_id")
    private String segmentId;
    
    @Column(name = "device_type")
    private String deviceType;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "location_country")
    private String locationCountry;
    
    @Column(name = "location_city")
    private String locationCity;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public NotificationAnalytics() {}
    
    public NotificationAnalytics(Long notificationId, Long userId, DeliveryChannel channel) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.channel = channel;
    }
    
    // Business Logic Methods
    public void markAsSent() {
        this.status = DeliveryStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.deliveryAttempts++;
    }
    
    public void markAsDelivered() {
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
    
    public void markAsRead() {
        this.status = DeliveryStatus.READ;
        this.readAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String reason) {
        this.status = DeliveryStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
        this.deliveryAttempts++;
    }
    
    public void markAsBounced() {
        this.status = DeliveryStatus.BOUNCED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = "Message bounced";
    }
    
    public void markAsUnsubscribed() {
        this.status = DeliveryStatus.UNSUBSCRIBED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = "User unsubscribed";
    }
    
    public boolean isDelivered() {
        return status == DeliveryStatus.DELIVERED || status == DeliveryStatus.READ;
    }
    
    public boolean isFailed() {
        return status == DeliveryStatus.FAILED || status == DeliveryStatus.BOUNCED || status == DeliveryStatus.UNSUBSCRIBED;
    }
    
    public boolean isRead() {
        return status == DeliveryStatus.READ;
    }
    
    public long getDeliveryTimeMs() {
        if (sentAt != null && deliveredAt != null) {
            return java.time.Duration.between(sentAt, deliveredAt).toMillis();
        }
        return 0;
    }
    
    public long getReadTimeMs() {
        if (deliveredAt != null && readAt != null) {
            return java.time.Duration.between(deliveredAt, readAt).toMillis();
        }
        return 0;
    }
    
    public long getTotalTimeMs() {
        if (sentAt != null && readAt != null) {
            return java.time.Duration.between(sentAt, readAt).toMillis();
        }
        return 0;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public DeliveryChannel getChannel() { return channel; }
    public void setChannel(DeliveryChannel channel) { this.channel = channel; }
    
    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    
    public LocalDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(LocalDateTime failedAt) { this.failedAt = failedAt; }
    
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    
    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }
    
    public Integer getDeliveryAttempts() { return deliveryAttempts; }
    public void setDeliveryAttempts(Integer deliveryAttempts) { this.deliveryAttempts = deliveryAttempts; }
    
    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
    
    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }
    
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    
    public String getSegmentId() { return segmentId; }
    public void setSegmentId(String segmentId) { this.segmentId = segmentId; }
    
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getLocationCountry() { return locationCountry; }
    public void setLocationCountry(String locationCountry) { this.locationCountry = locationCountry; }
    
    public String getLocationCity() { return locationCity; }
    public void setLocationCity(String locationCity) { this.locationCity = locationCity; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
