package com.xypay.xypay.config;

import com.xypay.xypay.domain.AuditLog;
import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.SecurityAlert;
import com.xypay.xypay.service.SecurityUtilityService;
import com.xypay.xypay.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * Security interceptor for request auditing and session tracking.
 * Equivalent to Django's security middleware.
 */
@Component
public class SecurityInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityInterceptor.class);
    
    @Autowired
    private SecurityUtilityService securityUtilityService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        try {
            // Get user from security context
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = null;
            
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String username = auth.getName();
                user = userRepository.findByUsername(username).orElse(null);
            }
            
            // Get request details
            String ipAddress = securityUtilityService.getClientIp(
                request.getHeader("X-Forwarded-For"), 
                request.getRemoteAddr()
            );
            String userAgent = request.getHeader("User-Agent");
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String sessionId = request.getSession().getId();
            
            // Track user session if authenticated
            if (user != null) {
                securityUtilityService.trackUserSession(user, sessionId, ipAddress, userAgent);
            }
            
            // Log API access for sensitive endpoints
            if (isSensitiveEndpoint(uri)) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("method", method);
                metadata.put("uri", uri);
                metadata.put("session_id", sessionId);
                metadata.put("query_string", request.getQueryString());
                
                securityUtilityService.logAuditEvent(
                    user,
                    AuditLog.ActionType.API_ACCESS,
                    String.format("%s %s", method, uri),
                    AuditLog.SeverityLevel.LOW,
                    ipAddress,
                    userAgent,
                    "HttpRequest",
                    null,
                    metadata
                );
            }
            
            // Check for suspicious activity
            if (user != null) {
                var suspiciousResult = securityUtilityService.checkSuspiciousActivity(user, ipAddress, "api_access");
                if (suspiciousResult.isSuspicious()) {
                    logger.warn("Suspicious activity detected for user {}: {}", 
                        user.getUsername(), String.join(", ", suspiciousResult.getReasons()));
                    
                    // Create security alert for suspicious activity
                    securityUtilityService.createSecurityAlert(
                        SecurityAlert.AlertType.SUSPICIOUS_ACTIVITY,
                        AuditLog.SeverityLevel.MEDIUM,
                        "Suspicious API Access",
                        "Suspicious activity detected: " + String.join(", ", suspiciousResult.getReasons()),
                        user,
                        ipAddress
                    );
                }
            }
            
        } catch (Exception e) {
            logger.error("Error in security interceptor: {}", e.getMessage());
            // Don't block the request due to interceptor errors
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) throws Exception {
        try {
            // Log failed requests
            if (response.getStatus() >= 400) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User user = null;
                
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                    String username = auth.getName();
                    user = userRepository.findByUsername(username).orElse(null);
                }
                
                String ipAddress = securityUtilityService.getClientIp(
                    request.getHeader("X-Forwarded-For"), 
                    request.getRemoteAddr()
                );
                String userAgent = request.getHeader("User-Agent");
                
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("method", request.getMethod());
                metadata.put("uri", request.getRequestURI());
                metadata.put("status_code", response.getStatus());
                metadata.put("session_id", request.getSession().getId());
                
                AuditLog.ActionType actionType = response.getStatus() == 401 ? 
                    AuditLog.ActionType.LOGIN_FAILED : AuditLog.ActionType.API_ERROR;
                
                AuditLog.SeverityLevel severity = response.getStatus() >= 500 ? 
                    AuditLog.SeverityLevel.HIGH : AuditLog.SeverityLevel.MEDIUM;
                
                securityUtilityService.logAuditEvent(
                    user,
                    actionType,
                    String.format("HTTP %d: %s %s", response.getStatus(), request.getMethod(), request.getRequestURI()),
                    severity,
                    ipAddress,
                    userAgent,
                    "HttpResponse",
                    null,
                    metadata
                );
            }
            
        } catch (Exception e) {
            logger.error("Error in security interceptor afterCompletion: {}", e.getMessage());
        }
    }
    
    /**
     * Check if endpoint is sensitive and requires auditing.
     */
    private boolean isSensitiveEndpoint(String uri) {
        return uri.startsWith("/api/auth/") ||
               uri.startsWith("/api/user/") ||
               uri.startsWith("/api/kyc/") ||
               uri.startsWith("/api/wallet/") ||
               uri.startsWith("/api/transfer/") ||
               uri.startsWith("/api/admin/") ||
               uri.contains("/pin/") ||
               uri.contains("/otp/") ||
               uri.contains("/verify");
    }
}
