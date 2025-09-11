package com.xypay.xypay.controller;

import com.xypay.xypay.service.UserCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/cleanup")
public class UserCleanupController {
    
    @Autowired
    private UserCleanupService userCleanupService;
    
    /**
     * Manually trigger the cleanup of expired unverified users
     * @return Result of the cleanup operation
     */
    @PostMapping("/expired-users")
    public ResponseEntity<String> cleanupExpiredUsers() {
        try {
            String result = userCleanupService.deleteExpiredUnverifiedUsers();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error during cleanup: " + e.getMessage());
        }
    }
}