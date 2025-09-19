package com.xypay.xypay.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions", indexes = {
    @Index(name = "idx_user_session_user", columnList = "user_id"),
    @Index(name = "idx_user_session_key", columnList = "session_key"),
    @Index(name = "idx_user_session_created", columnList = "created_at")
})
public class UserSession extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "session_key", unique = true, nullable = false, length = 40)
    private String sessionKey;
    
    @Column(name = "ip_address", nullable = false)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @UpdateTimestamp
    @Column(name = "last_activity", nullable = false)
    private LocalDateTime lastActivity;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Transient
    private String sessionDuration;
    
    // Constructors
    public UserSession() {}
    
    public UserSession(User user, String sessionKey, String ipAddress, String userAgent) {
        this.user = user;
        this.sessionKey = sessionKey;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.isActive = true;
    }
    
    // Business methods
    public void deactivate() {
        this.isActive = false;
    }
    
    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
    }
    
    public boolean isExpired(int sessionTimeoutMinutes) {
        return lastActivity.isBefore(LocalDateTime.now().minusMinutes(sessionTimeoutMinutes));
    }
    
    // Getters and Setters
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getSessionKey() {
        return sessionKey;
    }
    
    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
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
    
    
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    
    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getSessionDuration() {
        return sessionDuration;
    }
    
    public void setSessionDuration(String sessionDuration) {
        this.sessionDuration = sessionDuration;
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s - %s", 
            user != null ? user.getUsername() : "null", 
            ipAddress, 
            getCreatedAt());
    }
}
