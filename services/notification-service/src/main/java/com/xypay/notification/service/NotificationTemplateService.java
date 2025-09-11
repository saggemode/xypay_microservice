package com.xypay.notification.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xypay.notification.domain.NotificationTemplate;
import com.xypay.notification.domain.NotificationType;
import com.xypay.notification.repository.NotificationTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class NotificationTemplateService {
    
    @Autowired
    private NotificationTemplateRepository templateRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Create a new notification template
     */
    public NotificationTemplate createTemplate(NotificationTemplate template) {
        return templateRepository.save(template);
    }
    
    /**
     * Get template by key
     */
    public Optional<NotificationTemplate> getTemplateByKey(String templateKey) {
        return templateRepository.findByTemplateKey(templateKey);
    }
    
    /**
     * Get template by key and language
     */
    public Optional<NotificationTemplate> getTemplateByKeyAndLanguage(String templateKey, String language) {
        return templateRepository.findByTemplateKeyAndLanguage(templateKey, language);
    }
    
    /**
     * Get template for notification type and channel
     */
    public Optional<NotificationTemplate> getTemplateForNotification(NotificationType notificationType, 
                                                                    NotificationTemplate.TemplateChannel channel, 
                                                                    String language) {
        return templateRepository.findByNotificationTypeAndChannelAndLanguage(notificationType, channel, language);
    }
    
    /**
     * Get default template for channel and language
     */
    public List<NotificationTemplate> getDefaultTemplates(NotificationTemplate.TemplateChannel channel, String language) {
        return templateRepository.findDefaultTemplatesByChannelAndLanguage(channel, language);
    }
    
    /**
     * Process template with variables
     */
    public String processTemplate(String templateKey, Map<String, Object> variables) {
        return processTemplate(templateKey, "en", variables);
    }
    
    /**
     * Process template with variables and language
     */
    public String processTemplate(String templateKey, String language, Map<String, Object> variables) {
        Optional<NotificationTemplate> templateOpt = getTemplateByKeyAndLanguage(templateKey, language);
        
        if (templateOpt.isEmpty()) {
            // Fallback to default language
            templateOpt = getTemplateByKeyAndLanguage(templateKey, "en");
        }
        
        if (templateOpt.isEmpty()) {
            throw new RuntimeException("Template not found: " + templateKey);
        }
        
        NotificationTemplate template = templateOpt.get();
        return template.processTemplate(variables);
    }
    
    /**
     * Process template subject with variables
     */
    public String processTemplateSubject(String templateKey, Map<String, Object> variables) {
        return processTemplateSubject(templateKey, "en", variables);
    }
    
    /**
     * Process template subject with variables and language
     */
    public String processTemplateSubject(String templateKey, String language, Map<String, Object> variables) {
        Optional<NotificationTemplate> templateOpt = getTemplateByKeyAndLanguage(templateKey, language);
        
        if (templateOpt.isEmpty()) {
            templateOpt = getTemplateByKeyAndLanguage(templateKey, "en");
        }
        
        if (templateOpt.isEmpty()) {
            throw new RuntimeException("Template not found: " + templateKey);
        }
        
        NotificationTemplate template = templateOpt.get();
        return template.processSubject(variables);
    }
    
    /**
     * Process template HTML content with variables
     */
    public String processTemplateHtml(String templateKey, Map<String, Object> variables) {
        return processTemplateHtml(templateKey, "en", variables);
    }
    
    /**
     * Process template HTML content with variables and language
     */
    public String processTemplateHtml(String templateKey, String language, Map<String, Object> variables) {
        Optional<NotificationTemplate> templateOpt = getTemplateByKeyAndLanguage(templateKey, language);
        
        if (templateOpt.isEmpty()) {
            templateOpt = getTemplateByKeyAndLanguage(templateKey, "en");
        }
        
        if (templateOpt.isEmpty()) {
            throw new RuntimeException("Template not found: " + templateKey);
        }
        
        NotificationTemplate template = templateOpt.get();
        return template.processHtmlContent(variables);
    }
    
    /**
     * Get template variables as Map
     */
    public Map<String, Object> getTemplateVariables(String templateKey) {
        Optional<NotificationTemplate> templateOpt = getTemplateByKey(templateKey);
        
        if (templateOpt.isEmpty()) {
            return Map.of();
        }
        
        NotificationTemplate template = templateOpt.get();
        String variablesJson = template.getVariables();
        
        if (variablesJson == null || variablesJson.isEmpty()) {
            return Map.of();
        }
        
        try {
            return objectMapper.readValue(variablesJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
    
    /**
     * Get all templates by category
     */
    public List<NotificationTemplate> getTemplatesByCategory(NotificationTemplate.TemplateCategory category) {
        return templateRepository.findByCategoryOrderByName(category);
    }
    
    /**
     * Get all templates by notification type
     */
    public List<NotificationTemplate> getTemplatesByNotificationType(NotificationType notificationType) {
        return templateRepository.findByNotificationType(notificationType);
    }
    
    /**
     * Get all templates by channel
     */
    public List<NotificationTemplate> getTemplatesByChannel(NotificationTemplate.TemplateChannel channel) {
        return templateRepository.findByChannel(channel);
    }
    
    /**
     * Get all active templates
     */
    public List<NotificationTemplate> getActiveTemplates() {
        return templateRepository.findByIsActive(true);
    }
    
    /**
     * Get all templates by language
     */
    public List<NotificationTemplate> getTemplatesByLanguage(String language) {
        return templateRepository.findByLanguage(language);
    }
    
    /**
     * Update template
     */
    public NotificationTemplate updateTemplate(NotificationTemplate template) {
        return templateRepository.save(template);
    }
    
    /**
     * Deactivate template
     */
    public void deactivateTemplate(Long templateId) {
        Optional<NotificationTemplate> templateOpt = templateRepository.findById(templateId);
        if (templateOpt.isPresent()) {
            NotificationTemplate template = templateOpt.get();
            template.setIsActive(false);
            templateRepository.save(template);
        }
    }
    
    /**
     * Activate template
     */
    public void activateTemplate(Long templateId) {
        Optional<NotificationTemplate> templateOpt = templateRepository.findById(templateId);
        if (templateOpt.isPresent()) {
            NotificationTemplate template = templateOpt.get();
            template.setIsActive(true);
            templateRepository.save(template);
        }
    }
    
    /**
     * Delete template
     */
    public void deleteTemplate(Long templateId) {
        templateRepository.deleteById(templateId);
    }
    
    /**
     * Get template statistics
     */
    public Map<String, Object> getTemplateStatistics() {
        Map<String, Object> stats = Map.of(
            "totalTemplates", templateRepository.count(),
            "activeTemplates", templateRepository.countActiveTemplates(),
            "templatesByType", templateRepository.countByNotificationType(NotificationType.TRANSACTION),
            "templatesByChannel", templateRepository.countByChannel(NotificationTemplate.TemplateChannel.EMAIL)
        );
        
        return stats;
    }
}
