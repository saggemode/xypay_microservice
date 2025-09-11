package com.xypay.xypay.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_user_action", columnList = "user_id, action"),
    @Index(name = "idx_audit_severity", columnList = "severity")
})
public class AuditLog {
    
    public enum ActionType {
        USER_CREATED("user_created"),
        USER_UPDATED("user_updated"),
        USER_DELETED("user_deleted"),
        USER_VERIFIED("user_verified"),
        USER_SUSPENDED("user_suspended"),
        LOGIN_SUCCESS("login_success"),
        LOGIN_FAILED("login_failed"),
        PASSWORD_CHANGED("password_changed"),
        OTP_SENT("otp_sent"),
        OTP_VERIFIED("otp_verified"),
        ADMIN_ACTION("admin_action"),
        SECURITY_ALERT("security_alert"),
        API_ACCESS("api_access"),
        API_ERROR("api_error");
        
        private final String value;
        
        ActionType(String value) {
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
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private ActionType action;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private SeverityLevel severity = SeverityLevel.LOW;
    
    // For tracking specific objects using generic foreign key approach
    @Column(name = "content_type")
    private String contentType;
    
    @Column(name = "object_id")
    private Long objectId;
    
    // Additional metadata as JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    // Constructors
    public AuditLog() {}
    
    public AuditLog(User user, ActionType action, String description, SeverityLevel severity, 
                   String ipAddress, String userAgent) {
        this.user = user;
        this.action = action;
        this.description = description;
        this.severity = severity;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
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
    
    public User getUser() { 
        return user; 
    }
    
    public void setUser(User user) { 
        this.user = user; 
    }
    
    public ActionType getAction() { 
        return action; 
    }
    
    public void setAction(ActionType action) { 
        this.action = action; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public String getIpAddress() { 
        return ipAddress; 
    }
    
    public void setIpAddress(String ipAddress) { 
        this.ipAddress = ipAddress; 
    }
    
    public String getUserAgent() { 
        return userAgent; 
    }
    
    public void setUserAgent(String userAgent) { 
        this.userAgent = userAgent; 
    }
    
    public SeverityLevel getSeverity() { 
        return severity; 
    }
    
    public void setSeverity(SeverityLevel severity) { 
        this.severity = severity; 
    }
    
    public String getContentType() { 
        return contentType; 
    }
    
    public void setContentType(String contentType) { 
        this.contentType = contentType; 
    }
    
    public Long getObjectId() { 
        return objectId; 
    }
    
    public void setObjectId(Long objectId) { 
        this.objectId = objectId; 
    }
    
    public Map<String, Object> getMetadata() { 
        return metadata; 
    }
    
    public void setMetadata(Map<String, Object> metadata) { 
        this.metadata = metadata; 
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s - %s", 
            timestamp, 
            user != null ? user.getUsername() : "system", 
            action);
    }
}