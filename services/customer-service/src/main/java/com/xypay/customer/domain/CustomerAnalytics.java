package com.xypay.customer.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_analytics")
public class CustomerAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
    
    @Column(name = "login_count")
    private Integer loginCount = 0;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "session_duration_minutes")
    private Long totalSessionDurationMinutes = 0L;
    
    @Column(name = "transaction_count")
    private Integer transactionCount = 0;
    
    @Column(name = "total_transaction_amount")
    private Double totalTransactionAmount = 0.0;
    
    @Column(name = "average_transaction_amount")
    private Double averageTransactionAmount = 0.0;
    
    @Column(name = "support_ticket_count")
    private Integer supportTicketCount = 0;
    
    @Column(name = "feedback_count")
    private Integer feedbackCount = 0;
    
    @Column(name = "average_feedback_rating")
    private Double averageFeedbackRating = 0.0;
    
    @Column(name = "kyc_completion_percentage")
    private Double kycCompletionPercentage = 0.0;
    
    @Column(name = "risk_score")
    private Double riskScore = 0.0;
    
    @Column(name = "engagement_score")
    private Double engagementScore = 0.0;
    
    @Column(name = "lifetime_value")
    private Double lifetimeValue = 0.0;
    
    @Column(name = "churn_probability")
    private Double churnProbability = 0.0;
    
    @Column(name = "preferred_channel")
    private String preferredChannel;
    
    @Column(name = "device_type")
    private String deviceType;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "timezone")
    private String timezone;
    
    @Column(name = "language_preference")
    private String languagePreference;
    
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;
    
    @Column(name = "activity_frequency")
    private String activityFrequency; // Daily, Weekly, Monthly, Rarely
    
    @Column(name = "feature_usage")
    private String featureUsage; // JSON string of feature usage stats
    
    @Column(name = "conversion_funnel_stage")
    private String conversionFunnelStage;
    
    @Column(name = "segmentation_tags")
    private String segmentationTags; // Comma-separated tags
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public CustomerAnalytics() {}
    
    public CustomerAnalytics(User customer) {
        this.customer = customer;
    }
    
    // Business Logic Methods
    public void recordLogin() {
        this.loginCount++;
        this.lastLogin = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        updateEngagementScore();
    }
    
    public void recordSession(Long durationMinutes) {
        this.totalSessionDurationMinutes += durationMinutes;
        this.lastActivity = LocalDateTime.now();
        updateEngagementScore();
    }
    
    public void recordTransaction(Double amount) {
        this.transactionCount++;
        this.totalTransactionAmount += amount;
        this.averageTransactionAmount = this.totalTransactionAmount / this.transactionCount;
        this.lifetimeValue = this.totalTransactionAmount;
        updateEngagementScore();
    }
    
    public void recordSupportTicket() {
        this.supportTicketCount++;
        updateEngagementScore();
    }
    
    public void recordFeedback(Integer rating) {
        this.feedbackCount++;
        // Recalculate average rating
        if (this.feedbackCount == 1) {
            this.averageFeedbackRating = rating.doubleValue();
        } else {
            this.averageFeedbackRating = (this.averageFeedbackRating * (this.feedbackCount - 1) + rating) / this.feedbackCount;
        }
        updateEngagementScore();
    }
    
    public void updateKycCompletion(Double percentage) {
        this.kycCompletionPercentage = percentage;
        updateEngagementScore();
    }
    
    public void updateRiskScore(Double score) {
        this.riskScore = score;
    }
    
    public void updateChurnProbability(Double probability) {
        this.churnProbability = probability;
    }
    
    private void updateEngagementScore() {
        // Simple engagement score calculation
        double score = 0.0;
        
        // Login frequency (0-30 points)
        if (loginCount > 0) {
            score += Math.min(30, loginCount * 2);
        }
        
        // Transaction activity (0-25 points)
        if (transactionCount > 0) {
            score += Math.min(25, transactionCount * 5);
        }
        
        // Session duration (0-20 points)
        if (totalSessionDurationMinutes > 0) {
            score += Math.min(20, totalSessionDurationMinutes / 10);
        }
        
        // KYC completion (0-15 points)
        score += kycCompletionPercentage * 0.15;
        
        // Feedback participation (0-10 points)
        if (feedbackCount > 0) {
            score += Math.min(10, feedbackCount * 2);
        }
        
        this.engagementScore = Math.min(100, score);
    }
    
    public String getActivityFrequency() {
        if (loginCount == 0) return "Never";
        if (loginCount >= 30) return "Daily";
        if (loginCount >= 7) return "Weekly";
        if (loginCount >= 1) return "Monthly";
        return "Rarely";
    }
    
    public String getCustomerSegment() {
        if (lifetimeValue >= 10000) return "VIP";
        if (lifetimeValue >= 1000) return "Premium";
        if (lifetimeValue >= 100) return "Regular";
        return "New";
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    
    public Integer getLoginCount() { return loginCount; }
    public void setLoginCount(Integer loginCount) { this.loginCount = loginCount; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public Long getTotalSessionDurationMinutes() { return totalSessionDurationMinutes; }
    public void setTotalSessionDurationMinutes(Long totalSessionDurationMinutes) { this.totalSessionDurationMinutes = totalSessionDurationMinutes; }
    
    public Integer getTransactionCount() { return transactionCount; }
    public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }
    
    public Double getTotalTransactionAmount() { return totalTransactionAmount; }
    public void setTotalTransactionAmount(Double totalTransactionAmount) { this.totalTransactionAmount = totalTransactionAmount; }
    
    public Double getAverageTransactionAmount() { return averageTransactionAmount; }
    public void setAverageTransactionAmount(Double averageTransactionAmount) { this.averageTransactionAmount = averageTransactionAmount; }
    
    public Integer getSupportTicketCount() { return supportTicketCount; }
    public void setSupportTicketCount(Integer supportTicketCount) { this.supportTicketCount = supportTicketCount; }
    
    public Integer getFeedbackCount() { return feedbackCount; }
    public void setFeedbackCount(Integer feedbackCount) { this.feedbackCount = feedbackCount; }
    
    public Double getAverageFeedbackRating() { return averageFeedbackRating; }
    public void setAverageFeedbackRating(Double averageFeedbackRating) { this.averageFeedbackRating = averageFeedbackRating; }
    
    public Double getKycCompletionPercentage() { return kycCompletionPercentage; }
    public void setKycCompletionPercentage(Double kycCompletionPercentage) { this.kycCompletionPercentage = kycCompletionPercentage; }
    
    public Double getRiskScore() { return riskScore; }
    public void setRiskScore(Double riskScore) { this.riskScore = riskScore; }
    
    public Double getEngagementScore() { return engagementScore; }
    public void setEngagementScore(Double engagementScore) { this.engagementScore = engagementScore; }
    
    public Double getLifetimeValue() { return lifetimeValue; }
    public void setLifetimeValue(Double lifetimeValue) { this.lifetimeValue = lifetimeValue; }
    
    public Double getChurnProbability() { return churnProbability; }
    public void setChurnProbability(Double churnProbability) { this.churnProbability = churnProbability; }
    
    public String getPreferredChannel() { return preferredChannel; }
    public void setPreferredChannel(String preferredChannel) { this.preferredChannel = preferredChannel; }
    
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public String getLanguagePreference() { return languagePreference; }
    public void setLanguagePreference(String languagePreference) { this.languagePreference = languagePreference; }
    
    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    
    public String getFeatureUsage() { return featureUsage; }
    public void setFeatureUsage(String featureUsage) { this.featureUsage = featureUsage; }
    
    public String getConversionFunnelStage() { return conversionFunnelStage; }
    public void setConversionFunnelStage(String conversionFunnelStage) { this.conversionFunnelStage = conversionFunnelStage; }
    
    public String getSegmentationTags() { return segmentationTags; }
    public void setSegmentationTags(String segmentationTags) { this.segmentationTags = segmentationTags; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
