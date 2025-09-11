package com.xypay.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xypay.notification.domain.WebhookConfiguration;
import com.xypay.notification.repository.WebhookConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class WebhookService {
    
    @Autowired
    private WebhookConfigurationRepository webhookRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Create webhook configuration
     */
    public WebhookConfiguration createWebhookConfiguration(WebhookConfiguration webhook) {
        return webhookRepository.save(webhook);
    }
    
    /**
     * Get webhook configuration by ID
     */
    public Optional<WebhookConfiguration> getWebhookConfiguration(Long id) {
        return webhookRepository.findById(id);
    }
    
    /**
     * Get all active webhook configurations
     */
    public List<WebhookConfiguration> getActiveWebhookConfigurations() {
        return webhookRepository.findByIsActiveTrue();
    }
    
    /**
     * Get webhook configurations by event
     */
    public List<WebhookConfiguration> getWebhookConfigurationsByEvent(String event) {
        return webhookRepository.findByEventsContainingAndIsActiveTrue(event);
    }
    
    /**
     * Update webhook configuration
     */
    public WebhookConfiguration updateWebhookConfiguration(WebhookConfiguration webhook) {
        return webhookRepository.save(webhook);
    }
    
    /**
     * Delete webhook configuration
     */
    public void deleteWebhookConfiguration(Long id) {
        webhookRepository.deleteById(id);
    }
    
    /**
     * Trigger webhook for event
     */
    @Async
    public void triggerWebhook(String event, Map<String, Object> payload) {
        List<WebhookConfiguration> webhooks = getWebhookConfigurationsByEvent(event);
        
        for (WebhookConfiguration webhook : webhooks) {
            try {
                sendWebhookRequest(webhook, event, payload);
                webhook.incrementSuccessCount();
                webhook.clearLastError();
            } catch (Exception e) {
                webhook.setLastError(e.getMessage());
                webhook.incrementFailureCount();
            } finally {
                webhook.markAsTriggered();
                webhookRepository.save(webhook);
            }
        }
    }
    
    /**
     * Send webhook request
     */
    private void sendWebhookRequest(WebhookConfiguration webhook, String event, Map<String, Object> payload) {
        try {
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Webhook-Event", event);
            headers.set("X-Webhook-Timestamp", String.valueOf(System.currentTimeMillis()));
            
            // Add custom headers
            if (webhook.getHeaders() != null && !webhook.getHeaders().isEmpty()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, String> customHeaders = objectMapper.readValue(webhook.getHeaders(), Map.class);
                    customHeaders.forEach(headers::set);
                } catch (Exception e) {
                    // Log error but continue
                    System.err.println("Error parsing custom headers: " + e.getMessage());
                }
            }
            
            // Add signature if secret key is provided
            if (webhook.getSecretKey() != null && !webhook.getSecretKey().isEmpty()) {
                String signature = generateSignature(webhook.getSecretKey(), payload);
                headers.set("X-Webhook-Signature", signature);
            }
            
            // Prepare payload
            Map<String, Object> webhookPayload = new HashMap<>();
            webhookPayload.put("event", event);
            webhookPayload.put("timestamp", LocalDateTime.now().toString());
            webhookPayload.put("data", payload);
            webhookPayload.put("webhook_id", webhook.getId());
            
            String jsonPayload = objectMapper.writeValueAsString(webhookPayload);
            
            // Create request entity
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);
            
            // Send request
            ResponseEntity<String> response = restTemplate.exchange(
                webhook.getUrl(),
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            // Check response status
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Webhook request failed with status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error sending webhook request: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate HMAC signature for webhook payload
     */
    private String generateSignature(String secretKey, Map<String, Object> payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signature = mac.doFinal(jsonPayload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + Base64.getEncoder().encodeToString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | Exception e) {
            throw new RuntimeException("Error generating webhook signature: " + e.getMessage(), e);
        }
    }
    
    /**
     * Test webhook configuration
     */
    public boolean testWebhookConfiguration(Long webhookId) {
        Optional<WebhookConfiguration> webhookOpt = getWebhookConfiguration(webhookId);
        
        if (webhookOpt.isEmpty()) {
            return false;
        }
        
        WebhookConfiguration webhook = webhookOpt.get();
        
        try {
            // Send test payload
            Map<String, Object> testPayload = Map.of(
                "test", true,
                "message", "This is a test webhook",
                "timestamp", LocalDateTime.now().toString()
            );
            
            sendWebhookRequest(webhook, "test", testPayload);
            return true;
            
        } catch (Exception e) {
            webhook.setLastError(e.getMessage());
            webhook.incrementFailureCount();
            webhookRepository.save(webhook);
            return false;
        }
    }
    
    /**
     * Get webhook statistics
     */
    public Map<String, Object> getWebhookStatistics() {
        List<WebhookConfiguration> webhooks = webhookRepository.findAll();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_webhooks", webhooks.size());
        stats.put("active_webhooks", webhooks.stream().mapToLong(w -> w.getIsActive() ? 1 : 0).sum());
        stats.put("total_successes", webhooks.stream().mapToLong(WebhookConfiguration::getSuccessCount).sum());
        stats.put("total_failures", webhooks.stream().mapToLong(WebhookConfiguration::getFailureCount).sum());
        
        // Calculate overall success rate
        long totalRequests = webhooks.stream().mapToLong(w -> w.getSuccessCount() + w.getFailureCount()).sum();
        if (totalRequests > 0) {
            long totalSuccesses = webhooks.stream().mapToLong(WebhookConfiguration::getSuccessCount).sum();
            stats.put("overall_success_rate", (totalSuccesses * 100.0) / totalRequests);
        } else {
            stats.put("overall_success_rate", 0.0);
        }
        
        return stats;
    }
    
    /**
     * Get webhook health status
     */
    public Map<String, Object> getWebhookHealthStatus() {
        List<WebhookConfiguration> webhooks = webhookRepository.findAll();
        
        Map<String, Object> health = new HashMap<>();
        health.put("healthy_webhooks", webhooks.stream().mapToLong(w -> w.isHealthy() ? 1 : 0).sum());
        health.put("unhealthy_webhooks", webhooks.stream().mapToLong(w -> !w.isHealthy() ? 1 : 0).sum());
        
        // List unhealthy webhooks
        List<Map<String, Object>> unhealthyWebhooks = new ArrayList<>();
        for (WebhookConfiguration webhook : webhooks) {
            if (!webhook.isHealthy()) {
                Map<String, Object> webhookInfo = new HashMap<>();
                webhookInfo.put("id", webhook.getId());
                webhookInfo.put("name", webhook.getName());
                webhookInfo.put("url", webhook.getUrl());
                webhookInfo.put("success_rate", webhook.getSuccessRate());
                webhookInfo.put("last_error", webhook.getLastError());
                unhealthyWebhooks.add(webhookInfo);
            }
        }
        health.put("unhealthy_webhook_details", unhealthyWebhooks);
        
        return health;
    }
    
    /**
     * Retry failed webhook
     */
    @Async
    public void retryFailedWebhook(Long webhookId, String event, Map<String, Object> payload) {
        Optional<WebhookConfiguration> webhookOpt = getWebhookConfiguration(webhookId);
        
        if (webhookOpt.isPresent()) {
            WebhookConfiguration webhook = webhookOpt.get();
            
            if (webhook.shouldRetry()) {
                try {
                    sendWebhookRequest(webhook, event, payload);
                    webhook.incrementSuccessCount();
                    webhook.clearLastError();
                } catch (Exception e) {
                    webhook.setLastError(e.getMessage());
                    webhook.incrementFailureCount();
                } finally {
                    webhook.markAsTriggered();
                    webhookRepository.save(webhook);
                }
            }
        }
    }
}
