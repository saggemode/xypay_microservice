package com.xypay.xypay.controller;

import com.xypay.xypay.domain.UserPreferences;
import com.xypay.xypay.service.UserPreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
public class UserPreferencesController {
    @Autowired
    private UserPreferencesService userPreferencesService;

    @GetMapping
    public ResponseEntity<UserPreferences> getPreferences(@AuthenticationPrincipal UserDetails userDetails) {
        // Assume User entity has username as unique
        Long userId = getUserIdFromPrincipal(userDetails);
        UserPreferences prefs = userPreferencesService.getOrCreatePreferences(userId);
        return ResponseEntity.ok(prefs);
    }

    @PostMapping
    public ResponseEntity<UserPreferences> savePreferences(@AuthenticationPrincipal UserDetails userDetails,
                                                          @RequestBody UserPreferences preferences) {
        Long userId = getUserIdFromPrincipal(userDetails);
        preferences.setUserId(userId);
        UserPreferences saved = userPreferencesService.savePreferences(preferences);
        return ResponseEntity.ok(saved);
    }

    private Long getUserIdFromPrincipal(UserDetails userDetails) {
        // TODO: Implement lookup from username to userId
        // For now, return 1L as a placeholder
        return 1L;
    }
}
