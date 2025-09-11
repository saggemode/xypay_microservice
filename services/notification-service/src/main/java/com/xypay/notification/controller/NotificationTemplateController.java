package com.xypay.notification.controller;

import com.xypay.notification.domain.NotificationTemplate;
import com.xypay.notification.domain.NotificationType;
import com.xypay.notification.service.NotificationTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification-templates")
public class NotificationTemplateController {
    
    @Autowired
    private NotificationTemplateService templateService;
    
    @PostMapping
    public ResponseEntity<NotificationTemplate> createTemplate(@RequestBody NotificationTemplate template) {
        NotificationTemplate created = templateService.createTemplate(template);
        return ResponseEntity.ok(created);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NotificationTemplate> getTemplate(@PathVariable Long id) {
        return templateService.getTemplateByKey(id.toString())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/key/{templateKey}")
    public ResponseEntity<NotificationTemplate> getTemplateByKey(@PathVariable String templateKey) {
        return templateService.getTemplateByKey(templateKey)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/key/{templateKey}/language/{language}")
    public ResponseEntity<NotificationTemplate> getTemplateByKeyAndLanguage(
            @PathVariable String templateKey, @PathVariable String language) {
        return templateService.getTemplateByKeyAndLanguage(templateKey, language)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/process/{templateKey}")
    public ResponseEntity<String> processTemplate(
            @PathVariable String templateKey,
            @RequestBody Map<String, Object> variables) {
        try {
            String processedContent = templateService.processTemplate(templateKey, variables);
            return ResponseEntity.ok(processedContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing template: " + e.getMessage());
        }
    }
    
    @PostMapping("/process/{templateKey}/language/{language}")
    public ResponseEntity<String> processTemplateWithLanguage(
            @PathVariable String templateKey,
            @PathVariable String language,
            @RequestBody Map<String, Object> variables) {
        try {
            String processedContent = templateService.processTemplate(templateKey, language, variables);
            return ResponseEntity.ok(processedContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing template: " + e.getMessage());
        }
    }
    
    @PostMapping("/process-subject/{templateKey}")
    public ResponseEntity<String> processTemplateSubject(
            @PathVariable String templateKey,
            @RequestBody Map<String, Object> variables) {
        try {
            String processedSubject = templateService.processTemplateSubject(templateKey, variables);
            return ResponseEntity.ok(processedSubject);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing template subject: " + e.getMessage());
        }
    }
    
    @PostMapping("/process-html/{templateKey}")
    public ResponseEntity<String> processTemplateHtml(
            @PathVariable String templateKey,
            @RequestBody Map<String, Object> variables) {
        try {
            String processedHtml = templateService.processTemplateHtml(templateKey, variables);
            return ResponseEntity.ok(processedHtml);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing template HTML: " + e.getMessage());
        }
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<NotificationTemplate>> getTemplatesByCategory(
            @PathVariable NotificationTemplate.TemplateCategory category) {
        List<NotificationTemplate> templates = templateService.getTemplatesByCategory(category);
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/notification-type/{notificationType}")
    public ResponseEntity<List<NotificationTemplate>> getTemplatesByNotificationType(
            @PathVariable NotificationType notificationType) {
        List<NotificationTemplate> templates = templateService.getTemplatesByNotificationType(notificationType);
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/channel/{channel}")
    public ResponseEntity<List<NotificationTemplate>> getTemplatesByChannel(
            @PathVariable NotificationTemplate.TemplateChannel channel) {
        List<NotificationTemplate> templates = templateService.getTemplatesByChannel(channel);
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<NotificationTemplate>> getActiveTemplates() {
        List<NotificationTemplate> templates = templateService.getActiveTemplates();
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/language/{language}")
    public ResponseEntity<List<NotificationTemplate>> getTemplatesByLanguage(@PathVariable String language) {
        List<NotificationTemplate> templates = templateService.getTemplatesByLanguage(language);
        return ResponseEntity.ok(templates);
    }
    
    @GetMapping("/variables/{templateKey}")
    public ResponseEntity<Map<String, Object>> getTemplateVariables(@PathVariable String templateKey) {
        Map<String, Object> variables = templateService.getTemplateVariables(templateKey);
        return ResponseEntity.ok(variables);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<NotificationTemplate> updateTemplate(
            @PathVariable Long id, @RequestBody NotificationTemplate template) {
        template.setId(id);
        NotificationTemplate updated = templateService.updateTemplate(template);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateTemplate(@PathVariable Long id) {
        templateService.activateTemplate(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateTemplate(@PathVariable Long id) {
        templateService.deactivateTemplate(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTemplateStatistics() {
        Map<String, Object> stats = templateService.getTemplateStatistics();
        return ResponseEntity.ok(stats);
    }
}
