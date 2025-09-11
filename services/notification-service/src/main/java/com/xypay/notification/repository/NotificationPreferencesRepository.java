package com.xypay.notification.repository;

import com.xypay.notification.domain.NotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, Long> {
    
    Optional<NotificationPreferences> findByUserId(Long userId);
    
    @Query("SELECT p FROM NotificationPreferences p WHERE p.emailEnabled = true")
    List<NotificationPreferences> findUsersWithEmailEnabled();
    
    @Query("SELECT p FROM NotificationPreferences p WHERE p.smsEnabled = true")
    List<NotificationPreferences> findUsersWithSmsEnabled();
    
    @Query("SELECT p FROM NotificationPreferences p WHERE p.pushEnabled = true")
    List<NotificationPreferences> findUsersWithPushEnabled();
    
    @Query("SELECT p FROM NotificationPreferences p WHERE p.inAppEnabled = true")
    List<NotificationPreferences> findUsersWithInAppEnabled();
    
    @Query("SELECT p FROM NotificationPreferences p WHERE p.emailMarketing = true")
    List<NotificationPreferences> findUsersWithEmailMarketingEnabled();
    
    @Query("SELECT p FROM NotificationPreferences p WHERE p.smsMarketing = true")
    List<NotificationPreferences> findUsersWithSmsMarketingEnabled();
    
    @Query("SELECT p FROM NotificationPreferences p WHERE p.pushMarketing = true")
    List<NotificationPreferences> findUsersWithPushMarketingEnabled();
    
    @Query("SELECT p FROM NotificationPreferences p WHERE p.inAppMarketing = true")
    List<NotificationPreferences> findUsersWithInAppMarketingEnabled();
    
    @Query("SELECT p FROM NotificationPreferences p WHERE p.digestFrequency = :frequency")
    List<NotificationPreferences> findByDigestFrequency(@Param("frequency") String frequency);
    
    @Query("SELECT p FROM NotificationPreferences p WHERE p.language = :language")
    List<NotificationPreferences> findByLanguage(@Param("language") String language);
    
    @Query("SELECT p FROM NotificationPreferences p WHERE p.timezone = :timezone")
    List<NotificationPreferences> findByTimezone(@Param("timezone") String timezone);
    
    @Query("SELECT COUNT(p) FROM NotificationPreferences p WHERE p.emailEnabled = true")
    Long countUsersWithEmailEnabled();
    
    @Query("SELECT COUNT(p) FROM NotificationPreferences p WHERE p.smsEnabled = true")
    Long countUsersWithSmsEnabled();
    
    @Query("SELECT COUNT(p) FROM NotificationPreferences p WHERE p.pushEnabled = true")
    Long countUsersWithPushEnabled();
    
    @Query("SELECT COUNT(p) FROM NotificationPreferences p WHERE p.inAppEnabled = true")
    Long countUsersWithInAppEnabled();
    
    @Query("SELECT COUNT(p) FROM NotificationPreferences p WHERE p.emailMarketing = true")
    Long countUsersWithEmailMarketingEnabled();
    
    @Query("SELECT COUNT(p) FROM NotificationPreferences p WHERE p.smsMarketing = true")
    Long countUsersWithSmsMarketingEnabled();
    
    @Query("SELECT COUNT(p) FROM NotificationPreferences p WHERE p.pushMarketing = true")
    Long countUsersWithPushMarketingEnabled();
    
    @Query("SELECT COUNT(p) FROM NotificationPreferences p WHERE p.inAppMarketing = true")
    Long countUsersWithInAppMarketingEnabled();
}
