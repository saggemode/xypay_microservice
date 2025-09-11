package com.xypay.xypay.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/security")
public class SecurityTestController {
    
    /**
     * Test endpoint for admin actions
     */
    @PostMapping("/admin-action")
    public ResponseEntity<String> testAdminAction() {
        return ResponseEntity.ok("Admin action performed successfully");
    }
    
    /**
     * Test endpoint for profile view
     */
    @GetMapping("/accounts/profile/")
    public ResponseEntity<String> testProfileView() {
        return ResponseEntity.ok("Profile viewed successfully");
    }
    
    /**
     * Test endpoint for token request
     */
    @PostMapping("/api/token/")
    public ResponseEntity<String> testTokenRequest() {
        return ResponseEntity.ok("Token issued successfully");
    }
    
    /**
     * Test endpoint for verification request
     */
    @PostMapping("/accounts/request-verification/")
    public ResponseEntity<String> testVerificationRequest() {
        return ResponseEntity.ok("Verification requested successfully");
    }
}