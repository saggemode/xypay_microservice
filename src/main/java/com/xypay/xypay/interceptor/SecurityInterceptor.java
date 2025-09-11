package com.xypay.xypay.interceptor;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.service.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Component("legacySecurityInterceptor")
public class SecurityInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityInterceptor.class);
    
    @Autowired
    private SecurityService securityService;
    
    // Define important actions to log
    private static final Map<String, String> IMPORTANT_PATHS = new HashMap<>();
    
    static {
        IMPORTANT_PATHS.put("/admin/", "admin_access");
        IMPORTANT_PATHS.put("/accounts/profile/", "profile_view");
        IMPORTANT_PATHS.put("/accounts/request-verification/", "verification_request");
        IMPORTANT_PATHS.put("/accounts/verify/", "verification_attempt");
        IMPORTANT_PATHS.put("/api/token/", "token_request");
        IMPORTANT_PATHS.put("/api/token/refresh/", "token_refresh");
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Get client information
        String ipAddress = getClientIp(request);
        String userAgent = getUserAgent(request);
        
        // Store in request attributes for later use
        request.setAttribute("clientIp", ipAddress);
        request.setAttribute("clientUserAgent", userAgent);
        
        // Track authenticated user sessions
        Principal principal = request.getUserPrincipal();
        if (principal != null && request.getSession() != null && request.getSession().getId() != null) {
            // In a real implementation, we would track user sessions here
            // For now, we'll just log that we detected a session
            logger.debug("User session detected for user: {}", principal.getName());
        }
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // We'll do most of our logging in afterCompletion to ensure we have the full response
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Log user activity
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            logUserActivity(request, response, principal);
        }
    }
    
    private void logUserActivity(HttpServletRequest request, HttpServletResponse response, Principal principal) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String ipAddress = (String) request.getAttribute("clientIp");
        String userAgent = (String) request.getAttribute("clientUserAgent");
        
        User user = new User();
        user.setUsername(principal.getName());
        
        // Log admin actions
        if (path.startsWith("/admin/") && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            String action = "admin_action";
            String description = "Admin " + method + " action on " + path;
            String severity = "medium";
            
            securityService.logAuditEvent(user, action, description, severity, ipAddress, userAgent);
        }
        // Log authentication events
        else if (IMPORTANT_PATHS.containsKey(path)) {
            String action = IMPORTANT_PATHS.get(path);
            String description = "User accessed " + path + " via " + method;
            String severity = "low";
            
            securityService.logAuditEvent(user, action, description, severity, ipAddress, userAgent);
        }
        // Check for suspicious activity
        // Note: In a real implementation, we would integrate with the suspicious activity checking logic
        // For now, we'll just log that we would check for suspicious activity
        logger.debug("Would check for suspicious activity for user: {} from IP: {}", user.getUsername(), ipAddress);
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