package com.xypay.xypay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean auth;

    @Value("${spring.mail.properties.mail.smtp.ssl.enable}")
    private boolean sslEnable;

    @Value("${spring.mail.properties.mail.smtp.ssl.trust}")
    private String sslTrust;
    
    // Store for dynamic SMTP settings
    private static final Map<String, Object> dynamicSettings = new ConcurrentHashMap<>();

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // Use dynamic settings if available, otherwise use static configuration
        mailSender.setHost((String) dynamicSettings.getOrDefault("host", host));
        mailSender.setPort((Integer) dynamicSettings.getOrDefault("port", port));
        mailSender.setUsername((String) dynamicSettings.getOrDefault("username", username));
        mailSender.setPassword((String) dynamicSettings.getOrDefault("password", password));
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", dynamicSettings.getOrDefault("smtpAuth", auth));
        props.put("mail.smtp.ssl.enable", dynamicSettings.getOrDefault("sslEnabled", sslEnable));
        props.put("mail.smtp.ssl.trust", dynamicSettings.getOrDefault("sslTrust", sslTrust));
        props.put("mail.debug", "false");
        
        // Connection timeout settings
        props.put("mail.smtp.connectiontimeout", "10000"); // 10 seconds
        props.put("mail.smtp.timeout", "10000"); // 10 seconds
        props.put("mail.smtp.writetimeout", "10000"); // 10 seconds
        
        // Additional Gmail-specific settings for SSL
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.checkserveridentity", "true");
        
        return mailSender;
    }
    
    /**
     * Update SMTP settings dynamically
     * @param settings Map containing SMTP configuration
     */
    public static void updateSmtpSettings(Map<String, Object> settings) {
        dynamicSettings.clear();
        dynamicSettings.putAll(settings);
    }
    
    /**
     * Get current SMTP settings
     * @return Map of current SMTP settings
     */
    public static Map<String, Object> getCurrentSettings() {
        return new HashMap<>(dynamicSettings);
    }
}