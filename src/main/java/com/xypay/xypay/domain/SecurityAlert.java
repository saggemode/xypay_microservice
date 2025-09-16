package com.xypay.xypay.domain;

import com.xypay.xypay.enums.SecurityLevel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "security_alerts", indexes = {
    @Index(name = "idx_security_alert_user_read", columnList = "user_id, is_read"),
    @Index(name = "idx_security_alert_severity", columnList = "severity"),
    @Index(name = "idx_security_alert_created", columnList = "created_at")
})
public class SecurityAlert extends BaseEntity {
    
    public enum AlertType {
        LOGIN_ATTEMPT("login_attempt", "Login Attempt"),
        TRANSFER_ATTEMPT("transfer_attempt", "Transfer Attempt"),
        DEVICE_CHANGE("device_change", "Device Change"),
        LOCATION_CHANGE("location_change", "Location Change"),
        SUSPICIOUS_ACTIVITY("suspicious_activity", "Suspicious Activity"),
        LIMIT_EXCEEDED("limit_exceeded", "Limit Exceeded"),
        TWO_FA_REQUIRED("two_fa_required", "2FA Required");
        
        private final String code;
        private final String displayName;
        
        AlertType(String code, String displayName) {
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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", length = 50, nullable = false)
    private AlertType alertType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20, nullable = false)
    private SecurityLevel severity = SecurityLevel.MEDIUM;
    
    @Column(name = "title", length = 255, nullable = false)
    private String title;
    
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "location", length = 255)
    private String location;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    // Constructors
    public SecurityAlert() {}
    
    public SecurityAlert(User user, AlertType alertType, SecurityLevel severity, String title, String message) {
        this.user = user;
        this.alertType = alertType;
        this.severity = severity;
        this.title = title;
        this.message = message;
    }
    
    // Business methods
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
    
    public void markAsResolved() {
        this.isResolved = true;
    }
}
