package com.xypay.customer.controller;

import com.xypay.customer.domain.DataConsent;
import com.xypay.customer.service.GDPRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gdpr")
public class GDPRController {
    
    @Autowired
    private GDPRService gdprService;
    
    @GetMapping("/export/{customerId}")
    public ResponseEntity<Map<String, Object>> exportCustomerData(@PathVariable Long customerId) {
        Map<String, Object> exportData = gdprService.exportCustomerData(customerId);
        return ResponseEntity.ok(exportData);
    }
    
    @DeleteMapping("/delete/{customerId}")
    public ResponseEntity<Void> deleteCustomerData(@PathVariable Long customerId) {
        gdprService.deleteCustomerData(customerId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/portability/{customerId}")
    public ResponseEntity<Map<String, Object>> getDataPortability(@PathVariable Long customerId) {
        Map<String, Object> data = gdprService.getDataPortability(customerId);
        return ResponseEntity.ok(data);
    }
    
    @PostMapping("/consent/{customerId}/grant")
    public ResponseEntity<DataConsent> grantConsent(
            @PathVariable Long customerId,
            @RequestBody Map<String, String> consentData,
            HttpServletRequest request) {
        
        DataConsent.ConsentType consentType = DataConsent.ConsentType.valueOf(consentData.get("consentType").toUpperCase());
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String consentVersion = consentData.get("consentVersion");
        
        DataConsent consent = gdprService.grantConsent(customerId, consentType, ipAddress, userAgent, consentVersion);
        return ResponseEntity.ok(consent);
    }
    
    @PostMapping("/consent/{customerId}/withdraw")
    public ResponseEntity<DataConsent> withdrawConsent(
            @PathVariable Long customerId,
            @RequestBody Map<String, String> consentData) {
        
        DataConsent.ConsentType consentType = DataConsent.ConsentType.valueOf(consentData.get("consentType").toUpperCase());
        DataConsent consent = gdprService.withdrawConsent(customerId, consentType);
        return ResponseEntity.ok(consent);
    }
    
    @GetMapping("/consent/{customerId}")
    public ResponseEntity<List<DataConsent>> getCustomerConsents(@PathVariable Long customerId) {
        List<DataConsent> consents = gdprService.getCustomerConsents(customerId);
        return ResponseEntity.ok(consents);
    }
    
    @GetMapping("/consent/{customerId}/check")
    public ResponseEntity<Map<String, Boolean>> hasActiveConsent(
            @PathVariable Long customerId,
            @RequestParam String consentType) {
        
        DataConsent.ConsentType type = DataConsent.ConsentType.valueOf(consentType.toUpperCase());
        boolean hasConsent = gdprService.hasActiveConsent(customerId, type);
        return ResponseEntity.ok(Map.of("hasActiveConsent", hasConsent));
    }
    
    @PostMapping("/cleanup")
    public ResponseEntity<Void> cleanupExpiredData() {
        gdprService.cleanupExpiredData();
        return ResponseEntity.ok().build();
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
