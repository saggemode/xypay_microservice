package com.xypay.notification.controller;

import com.xypay.notification.domain.WebhookConfiguration;
import com.xypay.notification.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {
    
    @Autowired
    private WebhookService webhookService;
    
    @PostMapping
    public ResponseEntity<WebhookConfiguration> createWebhookConfiguration(@RequestBody WebhookConfiguration webhook) {
        WebhookConfiguration created = webhookService.createWebhookConfiguration(webhook);
        return ResponseEntity.ok(created);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<WebhookConfiguration> getWebhookConfiguration(@PathVariable Long id) {
        return webhookService.getWebhookConfiguration(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<WebhookConfiguration>> getActiveWebhookConfigurations() {
        List<WebhookConfiguration> webhooks = webhookService.getActiveWebhookConfigurations();
        return ResponseEntity.ok(webhooks);
    }
    
    @GetMapping("/event/{event}")
    public ResponseEntity<List<WebhookConfiguration>> getWebhookConfigurationsByEvent(@PathVariable String event) {
        List<WebhookConfiguration> webhooks = webhookService.getWebhookConfigurationsByEvent(event);
        return ResponseEntity.ok(webhooks);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<WebhookConfiguration> updateWebhookConfiguration(
            @PathVariable Long id, @RequestBody WebhookConfiguration webhook) {
        webhook.setId(id);
        WebhookConfiguration updated = webhookService.updateWebhookConfiguration(webhook);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebhookConfiguration(@PathVariable Long id) {
        webhookService.deleteWebhookConfiguration(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/test")
    public ResponseEntity<Boolean> testWebhookConfiguration(@PathVariable Long id) {
        boolean success = webhookService.testWebhookConfiguration(id);
        return ResponseEntity.ok(success);
    }
    
    @PostMapping("/trigger/{event}")
    public ResponseEntity<Void> triggerWebhook(
            @PathVariable String event, @RequestBody Map<String, Object> payload) {
        webhookService.triggerWebhook(event, payload);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/retry")
    public ResponseEntity<Void> retryFailedWebhook(
            @PathVariable Long id,
            @RequestParam String event,
            @RequestBody Map<String, Object> payload) {
        webhookService.retryFailedWebhook(id, event, payload);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getWebhookStatistics() {
        Map<String, Object> stats = webhookService.getWebhookStatistics();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getWebhookHealthStatus() {
        Map<String, Object> health = webhookService.getWebhookHealthStatus();
        return ResponseEntity.ok(health);
    }
}
