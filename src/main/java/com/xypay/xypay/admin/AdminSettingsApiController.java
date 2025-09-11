package com.xypay.xypay.admin;

import com.xypay.xypay.config.MailConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/admin/api")
@Validated
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERUSER')")
public class AdminSettingsApiController {

    private static final Logger logger = LoggerFactory.getLogger(AdminSettingsApiController.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.application.name:XyPay}")
    private String applicationName;
    
    // Simple in-memory storage for SMTP settings (fallback)
    private static final Map<String, Object> smtpConfigCache = new HashMap<>();

    /**
     * Test SMTP email configuration with enterprise validation and security
     */
    @PostMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmailConfiguration(
            @Valid @RequestBody @NotNull Map<String, Object> smtpConfig) {
        
        logger.info("Testing SMTP configuration for host: {}", smtpConfig.get("host"));
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate required fields
            validateSmtpConfiguration(smtpConfig);
            
            // Create a temporary mail sender with the provided configuration
            JavaMailSenderImpl testMailSender = createTestMailSender(smtpConfig);
            
            // Test the connection by sending a test email
            String testEmail = sendTestEmail(testMailSender, smtpConfig);
            
            response.put("success", true);
            response.put("message", "Test email sent successfully to: " + testEmail);
            response.put("timestamp", LocalDateTime.now());
            response.put("testEmailAddress", testEmail);
            
            logger.info("SMTP test successful for host: {}", smtpConfig.get("host"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("SMTP configuration validation failed: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Configuration validation failed: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            logger.error("SMTP test failed for host {}: {}", smtpConfig.get("host"), e.getMessage(), e);
            response.put("success", false);
            response.put("message", "SMTP test failed: " + getSecureErrorMessage(e));
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Save system settings with simple in-memory persistence
     */
    @PostMapping("/settings")
    public ResponseEntity<Map<String, Object>> saveSettings(
            @Valid @RequestBody @NotNull Map<String, Object> settings) {
        
        logger.info("Saving system settings");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> smtpSettings = (Map<String, Object>) settings.get("smtp");
            
            if (smtpSettings == null) {
                throw new IllegalArgumentException("SMTP settings are required");
            }
            
            // Validate SMTP configuration
            validateSmtpConfiguration(smtpSettings);
            
            // Save SMTP settings to cache (simple fallback)
            saveSmtpConfigurationToCache(smtpSettings);
            
            // Update MailConfig with new SMTP settings
            MailConfig.updateSmtpSettings(smtpSettings);
            
            response.put("success", true);
            response.put("message", "System settings saved successfully!");
            response.put("timestamp", LocalDateTime.now());
            response.put("configurationCount", smtpSettings.size());
            
            // Audit log (without sensitive data)
            logger.info("SMTP settings saved - Host: {}, Port: {}, Username: {}, SSL: {}", 
                smtpSettings.get("host"), 
                smtpSettings.get("port"), 
                smtpSettings.get("username"),
                smtpSettings.get("sslEnabled"));
            
        } catch (IllegalArgumentException e) {
            logger.warn("Settings validation failed: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Validation failed: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            logger.error("Failed to save settings: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to save settings: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get current system settings with security filtering
     */
    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSettings() {
        
        logger.debug("Loading system settings");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Load SMTP settings from cache
            Map<String, Object> smtpSettings = loadSmtpConfigurationFromCache();
            
            // Remove sensitive data from response (password)
            smtpSettings.remove("password");
            
            response.put("success", true);
            response.put("smtp", smtpSettings);
            response.put("timestamp", LocalDateTime.now());
            response.put("applicationName", applicationName);
            
            logger.debug("Settings loaded successfully");
            
        } catch (Exception e) {
            logger.error("Failed to load settings: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to load settings: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        return ResponseEntity.ok(response);
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    /**
     * Validate SMTP configuration parameters
     */
    private void validateSmtpConfiguration(Map<String, Object> smtpConfig) {
        if (smtpConfig.get("host") == null || smtpConfig.get("host").toString().trim().isEmpty()) {
            throw new IllegalArgumentException("SMTP host is required");
        }
        
        Object portObj = smtpConfig.get("port");
        if (portObj == null) {
            throw new IllegalArgumentException("SMTP port is required");
        }

        int port;
        if (portObj instanceof Integer) {
            port = (Integer) portObj;
        } else if (portObj instanceof String) {
            try {
                port = Integer.parseInt((String) portObj);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("SMTP port must be a valid number");
            }
        } else {
            throw new IllegalArgumentException("SMTP port must be a number");
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("SMTP port must be between 1 and 65535");
        }
        
        if (smtpConfig.get("username") == null || smtpConfig.get("username").toString().trim().isEmpty()) {
            throw new IllegalArgumentException("SMTP username is required");
        }
        
        if (smtpConfig.get("fromEmail") == null || smtpConfig.get("fromEmail").toString().trim().isEmpty()) {
            throw new IllegalArgumentException("From email address is required");
        }
        
        // Validate email format
        String fromEmail = smtpConfig.get("fromEmail").toString();
        if (!fromEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid from email address format");
        }
    }
    
    /**
     * Save SMTP configuration to in-memory cache
     */
    private void saveSmtpConfigurationToCache(Map<String, Object> smtpSettings) {
        try {
            smtpConfigCache.clear();
            smtpConfigCache.putAll(smtpSettings);
            smtpConfigCache.put("lastUpdated", LocalDateTime.now().toString());
            logger.info("SMTP configuration saved to cache successfully");
        } catch (Exception e) {
            logger.error("Failed to save SMTP configuration to cache: {}", e.getMessage());
            throw new RuntimeException("Failed to persist SMTP configuration", e);
        }
    }
    
    /**
     * Load SMTP configuration from cache
     */
    private Map<String, Object> loadSmtpConfigurationFromCache() {
        Map<String, Object> smtpSettings = new HashMap<>();
        
        if (smtpConfigCache.isEmpty()) {
            // Return default configuration
            smtpSettings.put("host", "smtp.gmail.com");
            smtpSettings.put("port", 587);
            smtpSettings.put("username", "admin@xypay.com");
            smtpSettings.put("fromEmail", "noreply@xypay.com");
            smtpSettings.put("sslEnabled", false);
            smtpSettings.put("smtpAuth", true);
        } else {
            smtpSettings.putAll(smtpConfigCache);
        }
        
        return smtpSettings;
    }
    
    /**
     * Create test mail sender with provided configuration
     */
    private JavaMailSenderImpl createTestMailSender(Map<String, Object> smtpConfig) {
        JavaMailSenderImpl testMailSender = new JavaMailSenderImpl();
        testMailSender.setHost((String) smtpConfig.get("host"));
        
        // 确保端口号是整数类型
        Object portObj = smtpConfig.get("port");
        if (portObj instanceof Integer) {
            testMailSender.setPort((Integer) portObj);
        } else if (portObj instanceof String) {
            try {
                testMailSender.setPort(Integer.parseInt((String) portObj));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("SMTP port must be a valid number");
            }
        } else {
            throw new IllegalArgumentException("SMTP port must be a number");
        }
        
        testMailSender.setUsername((String) smtpConfig.get("username"));
        testMailSender.setPassword((String) smtpConfig.get("password"));

        Properties props = testMailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", smtpConfig.getOrDefault("smtpAuth", true));
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.enable", smtpConfig.getOrDefault("sslEnabled", false));
        props.put("mail.smtp.ssl.trust", smtpConfig.get("host"));
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.debug", "false");
        
        return testMailSender;
    }
    
    /**
     * Send test email and return recipient address
     */
    private String sendTestEmail(JavaMailSenderImpl testMailSender, Map<String, Object> smtpConfig) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom((String) smtpConfig.get("fromEmail"));
        
        // Send test email to the configured username
        String testEmail = (String) smtpConfig.get("username");
        message.setTo(testEmail);
        message.setSubject(applicationName + " SMTP Configuration Test");
        message.setText(String.format(
            "This is a test email to verify your SMTP configuration is working correctly.\n\n" +
            "Configuration Details:\n" +
            "- Host: %s\n" +
            "- Port: %s\n" +
            "- SSL Enabled: %s\n" +
            "- Authentication: %s\n\n" +
            "If you receive this email, your SMTP settings are properly configured.\n\n" +
            "Sent from %s Admin Panel at %s",
            smtpConfig.get("host"),
            smtpConfig.get("port"),
            smtpConfig.getOrDefault("sslEnabled", false),
            smtpConfig.getOrDefault("smtpAuth", true),
            applicationName,
            LocalDateTime.now()
        ));

        testMailSender.send(message);
        return testEmail;
    }
    
    /**
     * Get secure error message without exposing sensitive information
     */
    private String getSecureErrorMessage(Exception e) {
        String message = e.getMessage();
        
        // Remove potentially sensitive information from error messages
        if (message != null) {
            message = message.replaceAll("password=[^\\s]+", "password=***");
            message = message.replaceAll("Password=[^\\s]+", "Password=***");
        }
        
        return message != null ? message : "An error occurred";
    }
}