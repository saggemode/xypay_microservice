package com.xypay.xypay.service;

import com.xypay.xypay.domain.UserPreferences;
import com.xypay.xypay.repository.UserPreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class UserPreferencesService {
    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    public UserPreferences getOrCreatePreferences(Long userId) {
        return userPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserPreferences prefs = new UserPreferences();
                    prefs.setUserId(userId);
                    return userPreferencesRepository.save(prefs);
                });
    }

    public UserPreferences savePreferences(UserPreferences preferences) {
        return userPreferencesRepository.save(preferences);
    }
}
