package com.xypay.notification.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_templates")
public class NotificationTemplate {
    
    public enum TemplateChannel {
        EMAIL("email", "Email"),
        SMS("sms", "SMS"),
        PUSH("push", "Push Notification"),
        IN_APP("in_app", "In-App Notification"),
        WEBHOOK("webhook", "Webhook");
        
        private final String value;
        private final String displayName;
        
        TemplateChannel(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum TemplateCategory {
        WELCOME("welcome", "Welcome"),
        TRANSACTION("transaction", "Transaction"),
        SECURITY("security", "Security"),
        KYC("kyc", "KYC"),
        SUPPORT("support", "Support"),
        MARKETING("marketing", "Marketing"),
        SYSTEM("system", "System"),
        REMINDER("reminder", "Reminder"),
        SAVINGS("savings", "Savings"),
        BANKING("banking", "Banking");
        
        private final String value;
        private final String displayName;
        
        TemplateCategory(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "template_name", nullable = false, unique = true)
    private String templateName;
    
    @Column(name = "template_key", nullable = false, unique = true)
    private String templateKey;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private TemplateChannel channel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private TemplateCategory category;
    
    @Column(name = "subject")
    private String subject;
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;
    
    @Column(name = "variables", columnDefinition = "TEXT")
    private String variables; // JSON string of available variables
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @Column(name = "language", length = 5)
    private String language = "en";
    
    @Column(name = "version")
    private String version = "1.0";
    
    @Column(name = "description")
    private String description;
    
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
    public NotificationTemplate() {}
    
    public NotificationTemplate(String templateName, String templateKey, NotificationType notificationType, 
                              TemplateChannel channel, TemplateCategory category, String content) {
        this.templateName = templateName;
        this.templateKey = templateKey;
        this.notificationType = notificationType;
        this.channel = channel;
        this.category = category;
        this.content = content;
    }
    
    // Business Logic Methods
    public String processTemplate(java.util.Map<String, Object> variables) {
        String processedContent = this.content;
        
        if (variables != null) {
            for (java.util.Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                processedContent = processedContent.replace(placeholder, value);
            }
        }
        
        return processedContent;
    }
    
    public String processSubject(java.util.Map<String, Object> variables) {
        if (this.subject == null) return null;
        
        String processedSubject = this.subject;
        
        if (variables != null) {
            for (java.util.Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                processedSubject = processedSubject.replace(placeholder, value);
            }
        }
        
        return processedSubject;
    }
    
    public String processHtmlContent(java.util.Map<String, Object> variables) {
        if (this.htmlContent == null) return null;
        
        String processedHtml = this.htmlContent;
        
        if (variables != null) {
            for (java.util.Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                processedHtml = processedHtml.replace(placeholder, value);
            }
        }
        
        return processedHtml;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    
    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }
    
    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }
    
    public TemplateChannel getChannel() { return channel; }
    public void setChannel(TemplateChannel channel) { this.channel = channel; }
    
    public TemplateCategory getCategory() { return category; }
    public void setCategory(TemplateCategory category) { this.category = category; }
    
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getHtmlContent() { return htmlContent; }
    public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }
    
    public String getVariables() { return variables; }
    public void setVariables(String variables) { this.variables = variables; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
