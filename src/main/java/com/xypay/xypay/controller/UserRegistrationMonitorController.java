package com.xypay.xypay.controller;

import com.xypay.xypay.service.UserRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/registration-monitor")
public class UserRegistrationMonitorController {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRegistrationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get basic statistics
        long totalRegistrations = userRegistrationService.getTotalRegistrationsCount();
        long verifiedUsers = userRegistrationService.getVerifiedUsersCount();
        long pendingVerifications = userRegistrationService.getPendingVerificationsCount();
        long todayRegistrations = userRegistrationService.getTodayRegistrationsCount();
        
        stats.put("totalRegistrations", totalRegistrations);
        stats.put("verifiedUsers", verifiedUsers);
        stats.put("pendingVerifications", pendingVerifications);
        stats.put("todayRegistrations", todayRegistrations);
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getRegistrationTrends() {
        Map<String, Object> trends = new HashMap<>();
        
        // Get registration trends for the last 30 days
        Map<String, Long> dailyRegistrations = userRegistrationService.getDailyRegistrationsLast30Days();
        trends.put("dailyRegistrations", dailyRegistrations);
        
        // Get verification status distribution
        Map<String, Long> verificationStatus = userRegistrationService.getVerificationStatusDistribution();
        trends.put("verificationStatus", verificationStatus);
        
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/recent-registrations")
    public ResponseEntity<Map<String, Object>> getRecentRegistrations() {
        Map<String, Object> response = new HashMap<>();
        
        // Get recent registrations with pagination
        var recentRegistrations = userRegistrationService.getRecentRegistrations(50);
        response.put("registrations", recentRegistrations);
        response.put("total", recentRegistrations.size());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending-support")
    public ResponseEntity<Map<String, Object>> getPendingSupportCases() {
        Map<String, Object> response = new HashMap<>();
        
        // Get pending support cases for CSO
        var pendingCases = userRegistrationService.getPendingSupportCases();
        response.put("cases", pendingCases);
        response.put("total", pendingCases.size());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/support-statistics")
    public ResponseEntity<Map<String, Object>> getSupportStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get support-related statistics
        long pendingSupport = userRegistrationService.getPendingSupportCount();
        long resolvedToday = userRegistrationService.getResolvedTodayCount();
        long newRegistrationsToday = userRegistrationService.getTodayRegistrationsCount();
        
        stats.put("pendingSupport", pendingSupport);
        stats.put("resolvedToday", resolvedToday);
        stats.put("newRegistrations", newRegistrationsToday);
        
        return ResponseEntity.ok(stats);
    }
}
