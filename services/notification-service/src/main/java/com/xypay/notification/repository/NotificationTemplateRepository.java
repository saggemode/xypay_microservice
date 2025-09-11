package com.xypay.notification.repository;

import com.xypay.notification.domain.NotificationTemplate;
import com.xypay.notification.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    
    Optional<NotificationTemplate> findByTemplateKey(String templateKey);
    
    List<NotificationTemplate> findByNotificationType(NotificationType notificationType);
    
    List<NotificationTemplate> findByChannel(NotificationTemplate.TemplateChannel channel);
    
    List<NotificationTemplate> findByCategory(NotificationTemplate.TemplateCategory category);
    
    List<NotificationTemplate> findByIsActive(Boolean isActive);
    
    List<NotificationTemplate> findByLanguage(String language);
    
    @Query("SELECT t FROM NotificationTemplate t WHERE t.templateKey = :templateKey AND t.language = :language AND t.isActive = true")
    Optional<NotificationTemplate> findByTemplateKeyAndLanguage(@Param("templateKey") String templateKey, @Param("language") String language);
    
    @Query("SELECT t FROM NotificationTemplate t WHERE t.notificationType = :notificationType AND t.channel = :channel AND t.isActive = true")
    List<NotificationTemplate> findByNotificationTypeAndChannel(@Param("notificationType") NotificationType notificationType, @Param("channel") NotificationTemplate.TemplateChannel channel);
    
    @Query("SELECT t FROM NotificationTemplate t WHERE t.notificationType = :notificationType AND t.channel = :channel AND t.language = :language AND t.isActive = true")
    Optional<NotificationTemplate> findByNotificationTypeAndChannelAndLanguage(@Param("notificationType") NotificationType notificationType, @Param("channel") NotificationTemplate.TemplateChannel channel, @Param("language") String language);
    
    @Query("SELECT t FROM NotificationTemplate t WHERE t.isDefault = true AND t.channel = :channel AND t.language = :language")
    List<NotificationTemplate> findDefaultTemplatesByChannelAndLanguage(@Param("channel") NotificationTemplate.TemplateChannel channel, @Param("language") String language);
    
    @Query("SELECT t FROM NotificationTemplate t WHERE t.category = :category AND t.isActive = true ORDER BY t.templateName ASC")
    List<NotificationTemplate> findByCategoryOrderByName(@Param("category") NotificationTemplate.TemplateCategory category);
    
    @Query("SELECT COUNT(t) FROM NotificationTemplate t WHERE t.notificationType = :notificationType")
    Long countByNotificationType(@Param("notificationType") NotificationType notificationType);
    
    @Query("SELECT COUNT(t) FROM NotificationTemplate t WHERE t.channel = :channel")
    Long countByChannel(@Param("channel") NotificationTemplate.TemplateChannel channel);
    
    @Query("SELECT COUNT(t) FROM NotificationTemplate t WHERE t.isActive = true")
    Long countActiveTemplates();
}
