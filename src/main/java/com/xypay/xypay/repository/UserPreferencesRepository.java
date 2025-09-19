package com.xypay.xypay.repository;

import com.xypay.xypay.domain.User;
import com.xypay.xypay.domain.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, UUID> {
    
    Optional<UserPreferences> findByUser(User user);
    
    Optional<UserPreferences> findByUserId(UUID userId);
    
    @Query("SELECT up FROM UserPreferences up WHERE up.spendSaveEnabled = true")
    List<UserPreferences> findUsersWithSpendSaveEnabled();
    
    @Query("SELECT up FROM UserPreferences up WHERE up.emailNotifications = true")
    List<UserPreferences> findUsersWithEmailNotifications();
    
    @Query("SELECT up FROM UserPreferences up WHERE up.smsNotifications = true")
    List<UserPreferences> findUsersWithSmsNotifications();
    
    @Query("SELECT up FROM UserPreferences up WHERE up.pushNotifications = true")
    List<UserPreferences> findUsersWithPushNotifications();
    
    @Query("SELECT up FROM UserPreferences up WHERE up.autoWithdrawalEnabled = true")
    List<UserPreferences> findUsersWithAutoWithdrawalEnabled();
    
    @Query("SELECT COUNT(up) FROM UserPreferences up WHERE up.spendSaveEnabled = true")
    Long countUsersWithSpendSaveEnabled();
    
    @Query("SELECT COUNT(up) FROM UserPreferences up WHERE up.emailNotifications = true")
    Long countUsersWithEmailNotifications();
    
    @Query("SELECT COUNT(up) FROM UserPreferences up WHERE up.smsNotifications = true")
    Long countUsersWithSmsNotifications();
    
    @Query("SELECT COUNT(up) FROM UserPreferences up WHERE up.pushNotifications = true")
    Long countUsersWithPushNotifications();
}