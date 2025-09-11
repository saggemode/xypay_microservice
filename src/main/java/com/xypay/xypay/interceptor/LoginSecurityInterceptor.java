package com.xypay.xypay.interceptor;

import com.xypay.xypay.domain.AuditLog;
import com.xypay.xypay.service.SecurityService;
import com.xypay.xypay.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class LoginSecurityInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginSecurityInterceptor.class);
    
    @Autowired
    private SecurityService securityService;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Monitor login attempts and detect brute force attacks
        if ("/api/token".equals(request.getRequestURI()) && "POST".equals(request.getMethod())) {
            String ipAddress = getClientIp(request);
            monitorLoginAttempt(request, ipAddress);
        }
        
        return true;
    }
    
    private void monitorLoginAttempt(HttpServletRequest request, String ipAddress) {
        try {
            // Get login credentials from request
            String username = null;
            
            // Try to get username from form parameters first
            username = request.getParameter("username");
            
            // If not found, try to get from JSON body
            if (username == null || username.isEmpty()) {
                // In a real implementation, we would parse the JSON body
                // For now, we'll just log that we detected a login attempt
                logger.debug("Login attempt detected from IP: {}", ipAddress);
            }
            
            if (username != null && !username.isEmpty()) {
                // Log the login attempt
                // Note: In a real implementation, we would check if the user exists
                // and perform additional security checks
                
                // Log successful or failed login attempt
                String action = "login_attempt";
                String description = "Login attempt for username '" + username + "' from " + ipAddress;
                String severity = "low";
                String userAgent = getUserAgent(request);
                
                // Create a temporary user object for logging
                com.xypay.xypay.domain.User user = new com.xypay.xypay.domain.User();
                user.setUsername(username);
                
                securityService.logAuditEvent(user, action, description, severity, ipAddress, userAgent);
                
                // Check for brute force attempts
                checkForBruteForceAttempts(ipAddress);
            }
        } catch (Exception e) {
            logger.error("Error monitoring login attempt: {}", e.getMessage());
        }
    }
    
    private void checkForBruteForceAttempts(String ipAddress) {
        try {
            // Check for multiple failed login attempts in the last 15 minutes
            LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
            List<AuditLog> recentFailures = auditLogRepository.findByActionAndIpAddressAndTimestampAfter(
                "login_failed", ipAddress, fifteenMinutesAgo);
            
            if (recentFailures.size() >= 10) {
                // Create security alert for brute force attack
                logger.warn("Brute force attack detected from IP: {}", ipAddress);
                
                // In a real implementation, we would create a security alert
                // For now, we'll just log the detection
                String description = "Multiple failed login attempts (" + recentFailures.size() + 
                                   ") from IP " + ipAddress;
                logger.info("Security alert: {}", description);
            }
        } catch (Exception e) {
            logger.error("Error checking for brute force attempts: {}", e.getMessage());
        }
    }
    
    /**
     * Get client IP address from request
     * @param request HttpServletRequest
     * @return Client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, get the first one
            String[] ips = xForwardedFor.split(",");
            return ips[0].trim();
        } else {
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            } else {
                return request.getRemoteAddr();
            }
        }
    }
    
    /**
     * Get user agent from request
     * @param request HttpServletRequest
     * @return User agent string
     */
    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}