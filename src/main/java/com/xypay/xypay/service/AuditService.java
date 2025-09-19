package com.xypay.xypay.service;

import com.xypay.xypay.domain.AuditLog;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.repository.AuditLogRepository;
import com.xypay.xypay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired(required = false) // Make this dependency optional
    private EventPublisherService eventPublisherService;
    
    /**
     * Log detailed audit action with entity information
     */
    public void logAction(String entityType, UUID entityId, String action, String payload, String username) {
        try {
            AuditLog auditLog = new AuditLog();
            
            // Find user by username
            User user = userRepository.findByUsername(username).orElse(null);
            auditLog.setUser(user);
            
            // Map string action to ActionType enum
            AuditLog.ActionType actionType = mapStringToActionType(action);
            auditLog.setAction(actionType);
            
            // Use description field for the main message
            String description = String.format("Action: %s on %s (ID: %s)", action, entityType, entityId);
            auditLog.setDescription(description);
            
            // Store entity information in metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("entityType", entityType);
            metadata.put("entityId", entityId);
            if (payload != null) {
                metadata.put("payload", payload);
            }
            auditLog.setMetadata(metadata);
            
            // Set content type and object ID for tracking
            auditLog.setContentType(entityType);
            auditLog.setObjectId(entityId);
            
            auditLog.setSeverity(AuditLog.SeverityLevel.LOW);
            auditLog.setTimestamp(LocalDateTime.now());
            
            auditLog = auditLogRepository.save(auditLog);
            
            // Only publish event if service is available
            if (eventPublisherService != null) {
                eventPublisherService.publishAuditEvent(auditLog);
            }
        } catch (Exception e) {
            logger.error("Failed to log audit action: {}", e.getMessage());
        }
    }
    
    /**
     * Simple audit logging method (compatible with AuditLogService)
     */
    public void log(String username, String action, String details) {
        try {
            AuditLog log = new AuditLog();
            
            // Find user by username
            User user = userRepository.findByUsername(username).orElse(null);
            log.setUser(user);
            
            // Map string action to ActionType enum
            AuditLog.ActionType actionType = mapStringToActionType(action);
            log.setAction(actionType);
            
            log.setDescription(details);
            log.setSeverity(AuditLog.SeverityLevel.LOW);
            log.setTimestamp(LocalDateTime.now());
            
            auditLogRepository.save(log);
        } catch (Exception e) {
            logger.error("Failed to log audit: {}", e.getMessage());
        }
    }
    
    /**
     * Map string action to ActionType enum
     */
    private AuditLog.ActionType mapStringToActionType(String action) {
        if (action == null) {
            return AuditLog.ActionType.API_ACCESS;
        }
        
        String actionLower = action.toLowerCase();
        
        if (actionLower.contains("login") && actionLower.contains("success")) {
            return AuditLog.ActionType.LOGIN_SUCCESS;
        } else if (actionLower.contains("login") && actionLower.contains("fail")) {
            return AuditLog.ActionType.LOGIN_FAILED;
        } else if (actionLower.contains("user") && actionLower.contains("creat")) {
            return AuditLog.ActionType.USER_CREATED;
        } else if (actionLower.contains("user") && actionLower.contains("updat")) {
            return AuditLog.ActionType.USER_UPDATED;
        } else if (actionLower.contains("user") && actionLower.contains("delet")) {
            return AuditLog.ActionType.USER_DELETED;
        } else if (actionLower.contains("verif")) {
            return AuditLog.ActionType.USER_VERIFIED;
        } else if (actionLower.contains("otp") && actionLower.contains("sent")) {
            return AuditLog.ActionType.OTP_SENT;
        } else if (actionLower.contains("otp") && actionLower.contains("verif")) {
            return AuditLog.ActionType.OTP_VERIFIED;
        } else if (actionLower.contains("password")) {
            return AuditLog.ActionType.PASSWORD_CHANGED;
        } else if (actionLower.contains("admin")) {
            return AuditLog.ActionType.ADMIN_ACTION;
        } else if (actionLower.contains("security") || actionLower.contains("alert")) {
            return AuditLog.ActionType.SECURITY_ALERT;
        } else if (actionLower.contains("error")) {
            return AuditLog.ActionType.API_ERROR;
        } else {
            return AuditLog.ActionType.API_ACCESS;
        }
    }
}