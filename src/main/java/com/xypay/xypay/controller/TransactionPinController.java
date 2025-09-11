package com.xypay.xypay.controller;

import com.xypay.xypay.service.TransactionPinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transaction-pin")
@CrossOrigin(origins = "*") // Configure appropriately for production
public class TransactionPinController {
    
    @Autowired
    private TransactionPinService transactionPinService;
    
    /**
     * Set transaction PIN (first time setup)
     * Example request body:
     * {
     *   "pin": "1234"
     * }
     */
    @PostMapping("/set/{userId}")
    public ResponseEntity<Map<String, Object>> setTransactionPin(
            @PathVariable Long userId, 
            @RequestBody Map<String, String> request) {
        
        String pin = request.get("pin");
        Map<String, Object> response = transactionPinService.setTransactionPin(userId, pin);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Update transaction PIN (requires old PIN)
     * Example request body:
     * {
     *   "old_pin": "1234",
     *   "new_pin": "5678"
     * }
     */
    @PutMapping("/update/{userId}")
    public ResponseEntity<Map<String, Object>> updateTransactionPin(
            @PathVariable Long userId, 
            @RequestBody Map<String, String> request) {
        
        String oldPin = request.get("old_pin");
        String newPin = request.get("new_pin");
        
        Map<String, Object> response = transactionPinService.updateTransactionPin(userId, oldPin, newPin);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Verify transaction PIN
     * Example request body:
     * {
     *   "pin": "1234"
     * }
     */
    @PostMapping("/verify/{userId}")
    public ResponseEntity<Map<String, Object>> verifyTransactionPin(
            @PathVariable Long userId, 
            @RequestBody Map<String, String> request) {
        
        String pin = request.get("pin");
        Map<String, Object> response = transactionPinService.verifyTransactionPin(userId, pin);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Check if user has transaction PIN set
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<Map<String, Object>> hasTransactionPin(@PathVariable Long userId) {
        Map<String, Object> response = transactionPinService.hasTransactionPin(userId);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Reset transaction PIN (Admin only)
     * Example request body:
     * {
     *   "admin_id": 1
     * }
     */
    @PostMapping("/reset/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERUSER')")
    public ResponseEntity<Map<String, Object>> resetTransactionPin(
            @PathVariable Long userId, 
            @RequestBody Map<String, Long> request) {
        
        Long adminId = request.get("admin_id");
        Map<String, Object> response = transactionPinService.resetTransactionPin(userId, adminId);
        
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "Transaction PIN API",
            "timestamp", System.currentTimeMillis()
        ));
    }
}