package com.xypay.xypay.service;

import com.xypay.xypay.domain.AuditLog;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    /**
     * Log an audit event for security tracking
     * @param user The user associated with the event
     * @param action The action performed
     * @param description Description of the event
     * @param severity Severity level (low, medium, high)
     * @param ipAddress IP address of the request
     * @param userAgent User agent string
     * @return The created audit log entry
     */
    public AuditLog logAuditEvent(User user, String action, String description, 
                                 String severity, String ipAddress, String userAgent) {
        try {
            // Convert string parameters to enum types
            AuditLog.ActionType actionType = convertStringToActionType(action);
            AuditLog.SeverityLevel severityLevel = convertStringToSeverityLevel(severity);
            
            AuditLog auditLog = new AuditLog(user, actionType, description, severityLevel, ipAddress, userAgent);
            // Note: timestamp is set automatically via @CreationTimestamp
            
            AuditLog saved = auditLogRepository.save(auditLog);
            logger.info("Audit event logged: {} - {} - {}", user.getUsername(), action, description);
            return saved;
        } catch (Exception e) {
            logger.error("Failed to log audit event: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Get client IP address from request headers
     * @param xForwardedFor X-Forwarded-For header value
     * @param remoteAddr Remote address
     * @return Client IP address
     */
    public String getClientIp(String xForwardedFor, String remoteAddr) {
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, get the first one
            String[] ips = xForwardedFor.split(",");
            return ips[0].trim();
        } else {
            return remoteAddr;
        }
    }
    
    /**
     * Check for suspicious user activity
     * @param user The user to check
     * @param ipAddress IP address of the request
     * @param action The action being performed
     * @return Array with [suspicious flag, reasons list]
     */
    public Object[] checkSuspiciousActivity(User user, String ipAddress, String action) {
        boolean suspicious = false;
        StringBuilder reasons = new StringBuilder();
        
        // Check for multiple failed login attempts
        if ("login_failed".equals(action)) {
            LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
            List<AuditLog> recentFailures = auditLogRepository.findByUserAndActionAndTimestampAfter(
                user, AuditLog.ActionType.LOGIN_FAILED.getValue(), fifteenMinutesAgo);
            
            if (recentFailures.size() >= 5) {
                suspicious = true;
                reasons.append("Multiple failed login attempts: ").append(recentFailures.size()).append("; ");
            }
        }
        
        // Note: For login from new IP and unusual activity patterns, 
        // we would need additional database entities and logic that 
        // aren't currently in the XY Pay codebase
        
        return new Object[]{suspicious, reasons.toString()};
    }
    
    /**
     * Convert string action to ActionType enum
     */
    private AuditLog.ActionType convertStringToActionType(String action) {
        if (action == null) return AuditLog.ActionType.API_ACCESS;
        
        switch (action.toLowerCase()) {
            case "user_created": return AuditLog.ActionType.USER_CREATED;
            case "user_updated": return AuditLog.ActionType.USER_UPDATED;
            case "user_deleted": return AuditLog.ActionType.USER_DELETED;
            case "user_verified": return AuditLog.ActionType.USER_VERIFIED;
            case "user_suspended": return AuditLog.ActionType.USER_SUSPENDED;
            case "login_success": return AuditLog.ActionType.LOGIN_SUCCESS;
            case "login_failed": return AuditLog.ActionType.LOGIN_FAILED;
            case "password_changed": return AuditLog.ActionType.PASSWORD_CHANGED;
            case "otp_sent": return AuditLog.ActionType.OTP_SENT;
            case "otp_verified": return AuditLog.ActionType.OTP_VERIFIED;
            case "admin_action": return AuditLog.ActionType.ADMIN_ACTION;
            case "security_alert": return AuditLog.ActionType.SECURITY_ALERT;
            case "api_error": return AuditLog.ActionType.API_ERROR;
            default: return AuditLog.ActionType.API_ACCESS;
        }
    }
    
    /**
     * Convert string severity to SeverityLevel enum
     */
    private AuditLog.SeverityLevel convertStringToSeverityLevel(String severity) {
        if (severity == null) return AuditLog.SeverityLevel.LOW;
        
        switch (severity.toLowerCase()) {
            case "low": return AuditLog.SeverityLevel.LOW;
            case "medium": return AuditLog.SeverityLevel.MEDIUM;
            case "high": return AuditLog.SeverityLevel.HIGH;
            case "critical": return AuditLog.SeverityLevel.CRITICAL;
            default: return AuditLog.SeverityLevel.LOW;
        }
    }
    
    /**
     * Cleanup old audit data for performance
     * @return Statistics about deleted data
     */
    public Object[] cleanupOldData() {
        try {
            // Keep audit logs for 1 year
            LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(365);
            List<AuditLog> oldAuditLogs = auditLogRepository.findByTimestampBefore(oneYearAgo);
            int auditLogsDeleted = oldAuditLogs.size();
            auditLogRepository.deleteAll(oldAuditLogs);
            
            // Return statistics
            return new Object[]{auditLogsDeleted};
        } catch (Exception e) {
            logger.error("Failed to cleanup old data: {}", e.getMessage());
            return new Object[]{0};
        }
    }
}